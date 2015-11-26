//
// Created by Matteo Paonessa on 09/10/15.
//

#include <setjmp.h>
#include <stdio.h>
#include <include/jpeglib.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <include/turbojpeg.h>
#include <math.h>

//TODO Error handling

#include <android/log.h>
#include <common.h>

typedef struct cclt_compress_parameters {
	int quality;
	int width;
	int height;
	int scaling_factor;
	char* output_folder;
	int color_space;
	int dct_method;
	int exif_copy;
	int lossless;
	char** input_files;
	int input_files_count;
	enum TJSAMP subsample;
	int recursive;
	int structure;
} cclt_compress_parameters;

cclt_compress_parameters initialize_compression_parameters() {
	cclt_compress_parameters par;
	
	par.quality = 0;
	par.width = 0;
	par.height = 0;
	par.scaling_factor = 100;
	par.color_space = TJPF_RGB;
	par.dct_method = TJFLAG_FASTDCT;
	par.output_folder = NULL;
	par.exif_copy = 0;
	par.lossless = 0;
	par.input_files_count = 0;
	par.recursive = 0;
	par.input_files = NULL;
	par.structure = 0;

	return par;
}

struct jpeg_decompress_struct cclt_get_markers(char* input) {
	FILE* fp;
	struct jpeg_decompress_struct einfo;
	struct jpeg_error_mgr eerr;
	einfo.err = jpeg_std_error(&eerr);

	jpeg_create_decompress(&einfo);

  	//Open the input file
	fp = fopen(input, "r");
	
	//Check for errors
	//TODO Use UNIX error messages
	if (fp == NULL) {
		LOGD("INPUT: Failed to open exif file \"%s\"\n", input);
		exit(-13);
	}

    //Create the IO istance for the input file
	jpeg_stdio_src(&einfo, fp);

    //Save EXIF info
	for (int m = 0; m < 16; m++) {
		jpeg_save_markers(&einfo, JPEG_APP0 + m, 0xFFFF);
	}

	jpeg_read_header(&einfo, TRUE);

	fclose(fp);

	return einfo;
}

int cclt_jpeg_optimize(char* input_file, char* output_file, int exif_flag, char* exif_src) {
	//TODO Bug on normal compress: the input file is a bogus long string
	// Happened with a (bugged) server connection
	//File pointer for both input and output
	FILE* fp;
	
	//Those will hold the input/output structs
	struct jpeg_decompress_struct srcinfo;
	struct jpeg_compress_struct dstinfo;

  	//Error handling
	struct jpeg_error_mgr jsrcerr, jdsterr;

	//Input/Output array coefficents
	jvirt_barray_ptr* src_coef_arrays;
	jvirt_barray_ptr* dst_coef_arrays;

	//Set errors and create the compress/decompress istances
	srcinfo.err = jpeg_std_error(&jsrcerr);
	jpeg_create_decompress(&srcinfo);
	dstinfo.err = jpeg_std_error(&jdsterr);
	jpeg_create_compress(&dstinfo);
	

  	//Open the input file
	fp = fopen(input_file, "r");
	
	//Check for errors
	//TODO Use UNIX error messages
	if (fp == NULL) {
		LOGD("INPUT: Failed to open file \"%s\"\n", input_file);
		return -1;
	}
	
	//Create the IO istance for the input file
	jpeg_stdio_src(&srcinfo, fp);

    //Save EXIF info
	if (exif_flag == 1) {
		for (int m = 0; m < 16; m++) {
			jpeg_save_markers(&srcinfo, JPEG_APP0 + m, 0xFFFF);
		}
	}

    //Read the input headers
	(void) jpeg_read_header(&srcinfo, TRUE);

	//Read input coefficents
	src_coef_arrays = jpeg_read_coefficients(&srcinfo);
  	//jcopy_markers_setup(&srcinfo, copyoption);

	//Copy parameters
	jpeg_copy_critical_parameters(&srcinfo, &dstinfo);

	//Set coefficents array to be the same
	dst_coef_arrays = src_coef_arrays;

	//We don't need the input file anymore
	fclose(fp);

	//Open the output one instead
	fp = fopen(output_file, "w+");
	//Check for errors
	//TODO Use UNIX error messages
	if (fp == NULL) {
		LOGD("OUTPUT: Failed to open file \"%s\"\n", output_file);
		return -2;
	}

	//CRITICAL - This is the optimization step
	dstinfo.optimize_coding = TRUE;
    //Progressive
	jpeg_simple_progression(&dstinfo);

	//Set the output file parameters
	jpeg_stdio_dest(&dstinfo, fp);

	//Actually write the coefficents
	jpeg_write_coefficients(&dstinfo, dst_coef_arrays);

    //Write EXIF
	if (exif_flag == 1) {
		if (strcmp(input_file, exif_src) == 0) {
			jcopy_markers_execute(&srcinfo, &dstinfo);
		} else {
    		//For standard compression EXIF data
			struct jpeg_decompress_struct einfo = cclt_get_markers(exif_src);
			jcopy_markers_execute(&einfo, &dstinfo);
			jpeg_destroy_decompress(&einfo);
		}
	}

    //Finish and free
	jpeg_finish_compress(&dstinfo);
	jpeg_destroy_compress(&dstinfo);
	(void) jpeg_finish_decompress(&srcinfo);
	jpeg_destroy_decompress(&srcinfo);

	//Close the output file
	fclose(fp);

	return 0;
}

void cclt_jpeg_compress(char* output_file, unsigned char* image_buffer, cclt_compress_parameters* pars) {
	FILE* fp;
	tjhandle tjCompressHandle;
	unsigned char* output_buffer;
	unsigned long output_size = 0;

	fp = fopen(output_file, "wb");

	//Check for errors
	//TODO Use UNIX error messages
	if (fp == NULL) {
       LOGD("OUTPUT: Failed to open output \"%s\"\n", output_file);
       return;
   }

   output_buffer = NULL;
   tjCompressHandle = tjInitCompress();

   //TODO Scale must be a power of 2. Can we resolve it?
   //TODO Error checks
   tjCompress2(tjCompressHandle,
       image_buffer,
       pars->width,
       0,
       pars->height,
       pars->color_space,
       &output_buffer,
       &output_size,
       pars->subsample,
       pars->quality,
       pars->dct_method);

   fwrite(output_buffer, 1, output_size, fp);

   fclose(fp);
   tjDestroy(tjCompressHandle);
   tjFree(output_buffer);

}

unsigned char* cclt_jpeg_decompress(char* fileName, cclt_compress_parameters* pars) {

	//TODO I/O Error handling

    FILE *file = NULL;
    int res = 0;
    long int sourceJpegBufferSize = 0;
    unsigned char* sourceJpegBuffer = NULL;
    tjhandle tjDecompressHandle;
    int fileWidth = 0, fileHeight = 0, jpegSubsamp = 0;

    //TODO No error checks here
    file = fopen(fileName, "rb");
    res = fseek(file, 0, SEEK_END);
    sourceJpegBufferSize = ftell(file);
    sourceJpegBuffer = tjAlloc(sourceJpegBufferSize);

    res = fseek(file, 0, SEEK_SET);
    res = fread(sourceJpegBuffer, (long)sourceJpegBufferSize, 1, file);
    tjDecompressHandle = tjInitDecompress();
    res = tjDecompressHeader2(tjDecompressHandle, sourceJpegBuffer, sourceJpegBufferSize, &fileWidth, &fileHeight, &jpegSubsamp);

    pars->width = ceil(fileWidth * ((double) pars->scaling_factor / 100));
    pars->height = ceil(fileHeight * ((double) pars->scaling_factor / 100));

    pars->subsample = jpegSubsamp;

    if (pars->subsample == TJSAMP_GRAY) {
    	pars->color_space = TJPF_GRAY;
    }

    unsigned char* temp = tjAlloc(pars->width * pars->height * tjPixelSize[pars->color_space]);

    res = tjDecompress2(tjDecompressHandle,
       sourceJpegBuffer,
       sourceJpegBufferSize,
       temp,
       pars->width,
       0,
       pars->height,
       pars->color_space,
       TJFLAG_ACCURATEDCT);

    tjDestroy(tjDecompressHandle);

    return temp;
}

void
Java_com_saerasoft_caesium_ImageCompressAsyncTask_CompressRoutine(JNIEnv *env, jobject obj, jstring input, jint exif, jint quality) {
	char *nativeIn = (*env)->GetStringUTFChars(env, input, 0);
	cclt_compress_parameters pars = initialize_compression_parameters();
	pars.quality = quality;
	switch(quality) {
		case 0:
			cclt_jpeg_optimize(nativeIn, nativeIn, (int) exif, nativeIn);
			break;
		default:
			cclt_jpeg_compress(nativeIn, cclt_jpeg_decompress(nativeIn, &pars), &pars);
			cclt_jpeg_optimize(nativeIn, nativeIn, (int) exif, nativeIn);
			break;
	}
	cclt_jpeg_optimize(nativeIn, nativeIn, (int) exif, nativeIn);
	(*env)->ReleaseStringUTFChars(env, input, nativeIn);
}