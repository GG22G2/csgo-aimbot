#include "pch.h"
#include <iostream>
#include <tchar.h>
#include <memory>
//#include <atlbase.h>
#include <dxgi.h>
#include <dxgi1_2.h>
#include <d3d11.h>
#include <DXGI1_5.h>
using namespace std;

boolean inited = false;

IDXGIFactory1* m_spDXGIFactory1;
IDXGIAdapter1* spAdapter;
IDXGIOutput* spDXGIOutput;
IDXGIOutputDuplication* spDXGIOutputDuplication;

//设备接口代表一个虚拟适配器;用于创建资源。
ID3D11Device* spD3D11Device;
//ID3D11DeviceContext接口表示生成渲染命令的设备上下文
ID3D11DeviceContext* spD3D11DeviceContext;




int capture_init() {
	if (inited) {
		return 1;
	}
	SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);

	HRESULT hr = CreateDXGIFactory1(__uuidof(IDXGIFactory1), (void**)(&m_spDXGIFactory1));
	

	//这里是枚举所有设备，因为我这里只有第一个接口可以找到IDXGIOutput，所有就没有枚举
	if (m_spDXGIFactory1->EnumAdapters1(0, &spAdapter) == DXGI_ERROR_NOT_FOUND) {
		return 0;
	}
	
	//这里是枚举所有设备，因为我这里只有一个，所有就没有枚举
	if ((spAdapter)->EnumOutputs(0, &spDXGIOutput) == DXGI_ERROR_NOT_FOUND) {
		return 0;
	}

	//描述Direct3D设备的一组特性,这里的值没意义，调用D3D11CreateDevice后，会重新给这个指针赋值
	D3D_FEATURE_LEVEL fl = D3D_FEATURE_LEVEL_11_0;
	// 参阅 https://docs.microsoft.com/zh-cn/windows/win32/api/d3d11/nf-d3d11-d3d11createdevice
	// 创建ID3D11Device等一些东西
	hr = D3D11CreateDevice(
		spAdapter,						/***/
		D3D_DRIVER_TYPE_UNKNOWN,		/***/
		NULL,							/** 软件栅格化的回调地址,这里不需要*/
		0,								/** 0代表单线程模式*/
		NULL,							/** pFeatureLevels ，使用默认特性数组*/
		0,								/** 上一个参数的长度，这里传0*/
		D3D11_SDK_VERSION,				/** 使用D3D11_SDK_VERSION版本*/
		&spD3D11Device,					/** 返回ID3D11Device的地址给这个指针*/
		&fl,							/** 返回从pFeatureLevels中，最终使用的特性，赋值给这个指针，测试返回的为D3D_FEATURE_LEVEL_11_0*/
		&spD3D11DeviceContext);			/** 返回ID3D11DeviceContext赋值给这个指针*/

	IDXGIOutput1* spDXGIOutput1 = (IDXGIOutput1*)spDXGIOutput;
	IDXGIDevice1* spDXGIDevice = (IDXGIDevice1*)spD3D11Device;

	IDXGIOutput5* output5;


	spDXGIOutput1->QueryInterface<IDXGIOutput5>(&output5);

	
	const DXGI_FORMAT formats[] = { DXGI_FORMAT_B8G8R8A8_UNORM };
	//获取IDXGIOutputDuplication，IDXGIOutputDuplication接口用来访问和操作被复制的桌面图像。
	// DuplicateOutput函数，从spDXGIOutput1创建一个桌面图像复制接口，
	// 根据官方文档，可以通过IDXGIOutput5::DuplicateOutput1 提高性能，待测试
	hr = output5->DuplicateOutput1(spDXGIDevice,0, ARRAYSIZE(formats), formats,&spDXGIOutputDuplication);

	if (FAILED(hr))
	{
		std::cout << hr << std::endl;
		return 0;
	}

	inited = true;
	return 1;
}

unsigned char* capture() {
	if (!inited) {
		return nullptr;
	}
	DXGI_OUTDUPL_FRAME_INFO frame_info;
	//IDXGIResource接口允许资源共享，并标识资源所在的内存。
	IDXGIResource* desktop_resource;
	HRESULT hr = spDXGIOutputDuplication->AcquireNextFrame(1000, &frame_info, &desktop_resource);

	// Timeout will return when desktop has no chane
	if (hr == DXGI_ERROR_WAIT_TIMEOUT || FAILED(hr)) return nullptr;

	ID3D11Texture2D* image;

	desktop_resource->QueryInterface(__uuidof(ID3D11Texture2D), reinterpret_cast<void**>(&image));

	desktop_resource->Release();
	desktop_resource = nullptr;

	D3D11_TEXTURE2D_DESC frame_desc;
	image->GetDesc(&frame_desc);

	ID3D11Texture2D* new_image = NULL;

	frame_desc.Usage = D3D11_USAGE_STAGING;
	frame_desc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;
	frame_desc.BindFlags = 0;
	frame_desc.MiscFlags = 0;
	frame_desc.MipLevels = 1;
	frame_desc.ArraySize = 1;
	frame_desc.SampleDesc.Count = 1;
	hr = spD3D11Device->CreateTexture2D(&frame_desc, NULL, &new_image);

	//这里在gpu中将图像进行了一次复制 , 看文档说CopyResource是异步方法，不知道哪里有同步操作
	spD3D11DeviceContext->CopyResource(new_image, image);
	spDXGIOutputDuplication->ReleaseFrame();
	image->Release();
	image = nullptr;

	//将图像从GPU映射到内存中
	IDXGISurface* dxgi_surface = NULL;
	hr = new_image->QueryInterface(__uuidof(IDXGISurface), (void**)(&dxgi_surface));
	new_image->Release();

	// DXGI_MAPPED_RECT的Pitch代表图像的宽度，假如1920像素，每个像素4通道，则Pitch等于7680
	// pBits指向表面的图像缓冲区的指针。
	DXGI_MAPPED_RECT rect;
	
	hr = dxgi_surface->Map(&rect, DXGI_MAP_READ);

	//因为要释放dxgi_surface，所以需要拷贝一次rect.pBits
	//memcpy(buffer, rect.pBits, 1920 * 1080 * 4);
	dxgi_surface->Unmap();
	dxgi_surface->Release();
	dxgi_surface = nullptr;

	return rect.pBits;
}

void capture_release() {
	m_spDXGIFactory1->Release();
	m_spDXGIFactory1 = nullptr;

	spAdapter->Release();
	spAdapter = nullptr;

	spDXGIOutput->Release();
	spDXGIOutput = nullptr;

	spDXGIOutputDuplication->Release();
	spDXGIOutputDuplication = nullptr;

	spD3D11Device->Release();
	spD3D11Device = nullptr;

	spD3D11DeviceContext->Release();
	spD3D11DeviceContext = nullptr;

	inited = false;
}

int main() {
	//capture_init();
	//unsigned char* buffer = new unsigned char[1920 * 1080 * 4];
	//capture(buffer);
	//capture_release();
	//mouseMove(100,0);

}