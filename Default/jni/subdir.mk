################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../jni/CoordinatesMain.c \
../jni/Egnos.c \
../jni/Ephemeris.c \
../jni/Fast_correction.c \
../jni/Ionosphere.c \
../jni/Long_correction.c \
../jni/Matrix.c \
../jni/Positioning.c \
../jni/Satellite.c \
../jni/Troposphere.c \
../jni/Utils.c 

C_DEPS += \
./jni/CoordinatesMain.d \
./jni/Egnos.d \
./jni/Ephemeris.d \
./jni/Fast_correction.d \
./jni/Ionosphere.d \
./jni/Long_correction.d \
./jni/Matrix.d \
./jni/Positioning.d \
./jni/Satellite.d \
./jni/Troposphere.d \
./jni/Utils.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I"/home/aanagnostopoulos/android-ndk-r10e/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/lib/gcc/arm-linux-androideabi/4.8/include" -I"/home/aanagnostopoulos/android-ndk-r10e/platforms/android-21/arch-arm/usr/include" -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


