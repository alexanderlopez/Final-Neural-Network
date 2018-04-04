package main;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ResourceHandler {
	
	public static final int TRAIN_SET = 0;
	public static final int TEST_SET = 1;
	private static final int MAGIC_LABEL = 2049;
	private static final int MAGIC_IMAGES = 2051;
	
	private int magicI;
	private int imageLength;
	private int rows;
	private int cols;
	
	private int magicL;
	private int labelsLength;
	
	private byte[] imageData;
	private byte[] labelData;
	
	public void init(int set) throws IOException {
		
		String useSetLabel = "";
		String useSetImage = "";
		
		switch (set) {
		case 0:
			useSetLabel = "train-labels-idx1-ubyte";
			useSetImage = "train-images-idx3-ubyte";
			break;
		case 1:
			useSetLabel = "t10k-labels-idx1-ubyte";
			useSetImage = "t10k-images-idx3-ubyte";
			break;
		default:
			return;
		}
		
		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(useSetImage);
		
		byte[] buffer = new byte[16];
		stream.read(buffer, 0, 16);
		
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.BIG_ENDIAN);
		magicI = bb.getInt();
		imageLength = bb.getInt();
		rows = bb.getInt();
		cols = bb.getInt();
		
		if (magicI != MAGIC_IMAGES)
			return;
		
		//SPECIAL READ TO GUARANTEE AL DATA IS STORED
		imageData = new byte[imageLength*rows*cols];
		
		byte[] buff = new byte[1024];
		int offset = 0;
		int read = 0;
		while ((read = stream.read(buff, 0, 1024)) != -1) {
			System.arraycopy(buff, 0, imageData, offset, read);
			offset += read;
		}
		
		stream = ClassLoader.getSystemClassLoader().getResourceAsStream(useSetLabel);
		
		buffer = new byte[8];
		stream.read(buffer, 0, 8);
		
		bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.BIG_ENDIAN);
		
		magicL = bb.getInt();
		labelsLength = bb.getInt();
		
		if (magicL != MAGIC_LABEL)
			return;
		
		//SPECIAL METHOD TO GUARANTEE ALL IS READ
		labelData = new byte[labelsLength];
		
		buff = new byte[1024];
		offset = 0;
		read = 0;
		while ((read = stream.read(buff, 0, 1024)) != -1) {
			System.arraycopy(buff, 0, labelData, offset, read);
			offset += read;
		}
		
		stream.close();
	}
	
	public int getDataLength() {
		if (imageLength != labelsLength)
			return -1;
		
		return imageLength;
	}
	
	public BufferedImage getImage(int resourceNum) {
		//Parameter resourceNum starts at 0
		BufferedImage output = new BufferedImage(rows,cols,BufferedImage.TYPE_BYTE_GRAY);
		output.setData(Raster.createRaster(output.getSampleModel(),
				new DataBufferByte(imageData, rows*cols,resourceNum*rows*cols), new Point(0,0)));
		
		return output;
	}
	
	public Matrix getImageData(int resourceNum) {
		//Parameter resourceNum starts at 0
		Matrix output = new Matrix(rows*cols, 1);
		
		for (int i = 0; i < output.getRows(); i++) {
			byte value = imageData[(resourceNum*rows*cols)+i];
			int data = Byte.toUnsignedInt(value);
			output.setValue(((double)data)/255.0d, i, 0);
		}
		
		return output;
	}
	
	public int getLabel(int resourceNum) {
		//Parameter resourceNum starts at 0
		return Byte.toUnsignedInt(labelData[resourceNum]);
	}
}
