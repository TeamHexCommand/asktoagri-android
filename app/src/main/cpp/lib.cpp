#include <jni.h>
#include <string>
extern "C"
JNIEXPORT jstring JNICALL
Java_in_hexcommand_asktoagri_util_shared_Keys_apiKey(JNIEnv *env, jobject thiz) {
    std::string api_key = "NTMzMTNjOTktODVjNy00NDJkLTk5NzAtN2ZlODkyOTBmNTdh";
    return env->NewStringUTF(api_key.c_str());
}