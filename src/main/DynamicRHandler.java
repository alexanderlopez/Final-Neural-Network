package main;

import java.io.ByteArrayInputStream;

public class DynamicRHandler {
	
	public static final int TRAIN_SET = 0;
	public static final int TEST_SET = 1;
	private static final int MAGIC_LABEL = 2049;
	private static final int MAGIC_IMAGES = 2051;
	
	ByteArrayInputStream inImage;
	ByteArrayInputStream inLabel;
	
	public void init(int set) {
		
	}
}
