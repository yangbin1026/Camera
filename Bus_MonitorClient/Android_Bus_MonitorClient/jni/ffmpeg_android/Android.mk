LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg

LOCAL_SRC_FILES := \
libavformat/allformats.c \
libswscale/options.c \
libswscale/rgb2rgb.c \
libswscale/swscale.c \
libswscale/swscale_unscaled.c \
libswscale/utils.c \
libswscale/yuv2rgb.c \
libavcodec/allcodecs.c \
libavcodec/arm/dsputil_arm.S \
libavcodec/arm/dsputil_init_arm.c \
libavcodec/arm/dsputil_init_armv5te.c \
libavcodec/arm/fft_fixed_init_arm.c \
libavcodec/arm/fft_init_arm.c \
libavcodec/arm/fmtconvert_init_arm.c \
libavcodec/arm/h264dsp_init_arm.c \
libavcodec/arm/h264pred_init_arm.c \
libavcodec/arm/jrevdct_arm.S \
libavcodec/arm/mpegvideo_arm.c \
libavcodec/arm/mpegvideo_armv5te.c \
libavcodec/arm/mpegvideo_armv5te_s.S \
libavcodec/arm/simple_idct_arm.S \
libavcodec/arm/simple_idct_armv5te.S \
libavcodec/audioconvert.c \
libavcodec/avpacket.c \
libavcodec/bitstream.c \
libavcodec/bitstream_filter.c \
libavcodec/cabac.c \
libavcodec/dsputil.c \
libavcodec/error_resilience.c \
libavcodec/faanidct.c \
libavcodec/fmtconvert.c \
libavcodec/golomb.c \
libavcodec/h264.c \
libavcodec/h264_cabac.c \
libavcodec/h264_cavlc.c \
libavcodec/h264_direct.c \
libavcodec/h264_loopfilter.c \
libavcodec/h264_ps.c \
libavcodec/h264_refs.c\
libavcodec/h264_sei.c \
libavcodec/h264dsp.c \
libavcodec/h264idct.c \
libavcodec/h264pred.c \
libavcodec/imgconvert.c \
libavcodec/inverse.c \
libavcodec/jrevdct.c \
libavcodec/mpegvideo.c \
libavcodec/options.c \
libavcodec/parser.c \
libavcodec/raw.c \
libavcodec/rawdec.c \
libavcodec/resample.c \
libavcodec/resample2.c \
libavcodec/simple_idct.c \
libavcodec/utils.c \
libavutil/adler32.c \
libavutil/aes.c \
libavutil/arm/cpu.c \
libavutil/audioconvert.c \
libavutil/avstring.c \
libavutil/base64.c \
libavutil/cpu.c \
libavutil/crc.c \
libavutil/des.c \
libavutil/dict.c \
libavutil/error.c \
libavutil/eval.c \
libavutil/fifo.c \
libavutil/file.c \
libavutil/imgutils.c \
libavutil/intfloat_readwrite.c \
libavutil/lfg.c \
libavutil/lls.c \
libavutil/log.c \
libavutil/lzo.c \
libavutil/mathematics.c \
libavutil/md5.c \
libavutil/mem.c \
libavutil/opt.c \
libavutil/parseutils.c \
libavutil/pixdesc.c \
libavutil/random_seed.c \
libavutil/rational.c \
libavutil/rc4.c \
libavutil/samplefmt.c \
libavutil/sha.c \
libavutil/tree.c \
libavutil/utils.c 

LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS += -std=c99 -DHAVE_AV_CONFIG_H -D_FILE_OFFSET_BITS=64 -D_ISOC99_SOURCE -D_LARGEFILE_SOURCE \
-D_LARGEFILE_SOURCE -DPI -I$(LOCAL_PATH)/libavcodec/arm \
-D_POSIX_C_SOURCE=200112 -D_XOPEN_SOURCE=600 -DPIC -DHAVE_AV_CONFIG_H -fPIC -fPIC

LOCAL_C_INCLUDES += $(LOCAL_PATH)/ \
$(LOCAL_PATH)/libavcodec/arm / \
$(LOCAL_PATH)/libavutil/


include $(BUILD_SHARED_LIBRARY)