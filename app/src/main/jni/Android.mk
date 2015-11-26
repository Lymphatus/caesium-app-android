LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libjpeg 
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libjpeg.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libturbojpeg 
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libturbojpeg.a

include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := caesium
LOCAL_SRC_FILES := caesium.c 
LOCAL_C_INCLUDES:= include
LOCAL_STATIC_LIBRARIES := jpeg turbojpeg 
LOCAL_CFLAGS += -std=c99
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)