package denoiser;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
	public static BufferedImage noising(BufferedImage X0, double sigma) {
		int width = X0.getWidth();
		int height = X0.getHeight();

		BufferedImage noisy = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster inputRaster = X0.getRaster();
		WritableRaster noisyRaster = noisy.getRaster();

		Random rand = new Random();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = inputRaster.getSample(x, y, 0);
				double noise = sigma * rand.nextGaussian();
				int noisyPixel = (int) Math.round(pixel + noise);

				noisyPixel = Math.max(0, Math.min(255, noisyPixel));

				noisyRaster.setSample(x, y, 0, noisyPixel);
			}
		}

		noisy.setData(noisyRaster);
		return noisy;
	}

	public static List<Patch> extractPatches(BufferedImage X, int s) {

		List<Patch> patches = new ArrayList<>();

		Raster raster = X.getRaster();

		int width = X.getWidth();

		int height = X.getHeight();

		for (int y = 0; y <= height - s; y++) {

			for (int x = 0; x <= width - s; x++) {

				double[] patchData = new double[s * s];

				int idx = 0;

				for (int dy = 0; dy < s; dy++) {

					for (int dx = 0; dx < s; dx++) {

						int val = raster.getSample(x + dx, y + dy, 0);

						patchData[idx++] = val;

					}

				}

				patches.add(new Patch(patchData, x, y));

			}

		}

		return patches;

	}

	public static BufferedImage reconstructPatches(List<Patch> patches, int height, int width) {
		int[][] sum = new int[height][width];
		int[][] count = new int[height][width];
		int s = (int) Math.sqrt(patches.get(0).data.length);
		for (Patch patch : patches) {
			double[] data = patch.data;
			int x0 = patch.positionX;
			int y0 = patch.positionY;
			int idx = 0;
			for (int dy = 0; dy < s; dy++) {
				for (int dx = 0; dx < s; dx++) {
					int x = x0 + dx;
					int y = y0 + dy;
					if (x < width && y < height) {
						sum[y][x] += (int) Math.round(data[idx]);
						count[y][x]++;
					}
					idx++;
				}
			}
		}

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = result.getRaster();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (count[y][x] > 0) {
					raster.setSample(x, y, 0, sum[y][x] / count[y][x]);
				} else {
					raster.setSample(x, y, 0, 0); // noir si non couvert
				}
			}
		}
		return result;
	}

	public static List<ImageZone> decoupeImage(BufferedImage image, int W, int pas) {
		List<ImageZone> zones = new ArrayList<>();

		int width = image.getWidth();
		int height = image.getHeight();

		for (int y = 0; y <= height - W; y += pas) {
			for (int x = 0; x <= width - W; x += pas) {
				BufferedImage sub = image.getSubimage(x, y, W, W);
				zones.add(new ImageZone(sub, x, y));
			}
		}

		// Ajout des zones de bordures droites (si image non divisible)
		if ((width - W) % pas != 0) {
			int x = width - W;
			for (int y = 0; y <= height - W; y += pas) {
				BufferedImage sub = image.getSubimage(x, y, W, W);
				zones.add(new ImageZone(sub, x, y));
			}
		}

		if ((height - W) % pas != 0) {
			int y = height - W;
			for (int x = 0; x <= width - W; x += pas) {
				BufferedImage sub = image.getSubimage(x, y, W, W);
				zones.add(new ImageZone(sub, x, y));
			}
		}

		// Coin bas droit si nécessaire
		if ((width - W) % pas != 0 && (height - W) % pas != 0) {
			BufferedImage sub = image.getSubimage(width - W, height - W, W, W);
			zones.add(new ImageZone(sub, width - W, height - W));
		}

		return zones;
	}

	public static List<VectorWithPosition> VectorPatchs(List<Patch> patches) {
		List<VectorWithPosition> result = new ArrayList<>();

		for (Patch patch : patches) {
			result.add(new VectorWithPosition(patch.toVector(), patch.positionX, patch.positionY));
		}

		return result;
	}

	public static BufferedImage recomposeFromZones(List<ImageZone> zones, int width, int height) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = result.getRaster();

		double[][] sum = new double[height][width];
		int[][] count = new int[height][width];

		for (ImageZone zone : zones) {
			BufferedImage img = zone.getImage();
			int offsetX = zone.getPosition()[0];
			int offsetY = zone.getPosition()[1];
			Raster zoneRaster = img.getRaster();

			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int val = zoneRaster.getSample(x, y, 0);
					sum[offsetY + y][offsetX + x] += val;
					count[offsetY + y][offsetX + x]++;
				}
			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int val = (count[y][x] > 0) ? (int) Math.round(sum[y][x] / count[y][x]) : 0;
				raster.setSample(x, y, 0, Math.max(0, Math.min(255, val)));
			}
		}

		result.setData(raster);
		return result;
	}

}