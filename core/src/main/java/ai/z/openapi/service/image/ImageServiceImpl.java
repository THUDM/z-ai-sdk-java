package ai.z.openapi.service.image;

import ai.z.openapi.ZaiClient;
import ai.z.openapi.api.images.ImagesApi;
import ai.z.openapi.utils.RequestSupplier;

/**
 * Image service implementation
 */
public class ImageServiceImpl implements ImageService {

	private final ZaiClient zAiClient;

	private final ImagesApi imagesApi;

	public ImageServiceImpl(ZaiClient zAiClient) {
		this.zAiClient = zAiClient;
		this.imagesApi = zAiClient.retrofit().create(ImagesApi.class);
	}

	@Override
	public ImageResponse createImage(CreateImageRequest createImageRequest) {
		RequestSupplier<CreateImageRequest, ImageResult> supplier = imagesApi::createImage;
		return this.zAiClient.executeRequest(createImageRequest, supplier, ImageResponse.class);
	}

}