
public class BlueFilter extends Filter{
	
	public BlueFilter(String name) {
		super(name);
	}
	
	
	
	public void apply(OFImage image) {
		
		   int height = image.getHeight();
	        int width = image.getWidth();
	        for(int y = 0; y < height; y++) {
	            for(int x = 0; x < width; x++) {
	            	image.setRGB(x, y, image.getPixel(x, y).getBlue());
	            }
	        }
	}
}