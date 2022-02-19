#include "pch.h"

void empty() {
}

int getIntFromPoint(void* p) {
	return ((int*)p)[0];
}

int getByteFromPoint(void* p) {
	return ((unsigned char*)p)[0];
}