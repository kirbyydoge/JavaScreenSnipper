package src;
import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ScreenSnipper extends Window {
	
	private static final long serialVersionUID = -8281976205273150247L;

	private class SnippingMouseListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent e) {
			setStartingPoint(e.getX(), e.getY());
		}
		
		public void mouseDragged(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());
			update(getGraphics());
		}
		
		public void mouseReleased(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());
			snipScreen(screenShotCache, startX, startY, endX, endY);
		}
		
	}
	
	private class TransferableImage implements Transferable {
		
		private Image img;
		
		public TransferableImage(Image img) {
			this.img = img;
		}

		@Override
		public Object getTransferData(DataFlavor flavor)  throws UnsupportedFlavorException, IOException {
			if(flavor.equals(DataFlavor.imageFlavor) && img != null) {
				return img;
			}
			else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] flavors = { DataFlavor.imageFlavor };
			return flavors;
		}
		
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == DataFlavor.imageFlavor;
		}
		
	}
	
	private static final int idx_X = 0;
	private static final int idx_Y = 1;

	private int[] screenShotCache;
	private int[] screenShadowCache;
	private BufferedImage screenBuffer;
	private Rectangle screenRect;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private String outputFolder;
	private double shadowRatio;
	
	public ScreenSnipper(Window owner, String outputFolder, double shadowRatio) {
		super(owner);
		
		this.outputFolder = outputFolder;
		this.shadowRatio = shadowRatio;
		
		startX = 0;
		startY = 0;
		endX = 0;
		endY = 0;
		screenRect = new Rectangle(0, 0, 0, 0);
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
		    screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
		}
		takeScreenShot();
		
		SnippingMouseListener sml = new SnippingMouseListener();
		addMouseListener(sml);
		addMouseMotionListener(sml);
		setAlwaysOnTop(true);
		setBounds(screenRect);
		setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(screenShotCache == null) {
			return;
		}
		if(screenBuffer == null) {
			screenBuffer = selectRegion(screenShotCache, screenShadowCache, screenRect.width, screenRect.height, startX, startY, endX, endY);
		}
		g.drawImage(screenBuffer, 0, 0, null);
	}
	
	@Override
	public void update(Graphics g) {
		screenBuffer = selectRegion(screenShotCache, screenShadowCache, screenRect.width, screenRect.height, startX, startY, endX, endY);
		paint(g);
	}
	
	private void takeScreenShot() {
		try {
			Robot robot = new Robot();
			BufferedImage screenShot = robot.createScreenCapture(screenRect);
			int width = screenRect.width;
			int height = screenRect.height;
			screenShotCache = new int[height * width];
			screenShot.getRGB(0, 0, width, height, screenShotCache, 0, width);
			screenShadowCache = getShadow(screenShotCache, width, height);
		}
		catch(AWTException e) {
			e.printStackTrace();
		}
	}
	
	private void snipScreen(int[] screenShotCache, int startX, int startY, int endX, int endY) {
		final int idx_X = 0;
		final int idx_Y = 1;
		int[] llc = { //Lower Left Corner
			Math.min(startX, endX),
			Math.min(startY, endY)
		};
		int[] urc = { //Upper Right Corner
			Math.max(startX, endX),
			Math.max(startY, endY)
		};
		int width = Math.abs(startX - endX);
		int height = Math.abs(startY - endY);
		if(width == 0 || height == 0) {
			return;
		}
		dispose();
		
		int[] pixels = new int[width * height];
		int pixelCounter = 0;
		for(int y = llc[idx_Y]; y < urc[idx_Y]; y++) {
			for(int x = llc[idx_X]; x < urc[idx_X]; x++) {
				pixels[pixelCounter++] = screenShotCache[y * screenRect.width + x];
			}
		}
		
		BufferedImage snip = null;
		snip = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = snip.getRaster();
		raster.setDataElements(0, 0, width, height, pixels);
		
		String fileName = String.format("%s%s.jpg", outputFolder, System.currentTimeMillis());
		File f = null;
		try {
			f = new File(fileName);
			ImageIO.write(snip, "jpg", f);
			Desktop.getDesktop().open(f);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			c.setContents(new TransferableImage(snip), new ClipboardOwner() {
				@Override
				public void lostOwnership(Clipboard clipboard, Transferable contents) {
					// Do nothing
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private BufferedImage selectRegion(int[] img, int[] img_shadow, int width, int height, int startX, int startY, int endX, int endY) {
		int[] copyImg = new int[width * height];
		int[] llc = { //Lower Left Corner
			Math.min(startX, endX),
			Math.min(startY, endY)
		};
		int[] urc = { //Upper Right Corner
			Math.max(startX, endX),
			Math.max(startY, endY)
		};
		
		System.arraycopy(img_shadow, 0, copyImg, 0, width * height);
		// TODO: Accelerate on GPU / with ThreadPool
		for(int y = llc[idx_Y]; y < urc[idx_Y]; y++) {
			for(int x = llc[idx_X]; x < urc[idx_X]; x++) {
				copyImg[y * width + x] = img[y * width + x];
			}
		}
		
		BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = finalImg.getRaster();
		raster.setDataElements(0, 0, width, height, copyImg);
		return finalImg;
	}
	
	private int[] getShadow(int[] img, int width, int height) {
		int[] copyImg = new int[width * height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int curPixel = img[y * width + x];
				copyImg[y * width + x] = makePixelDarker(curPixel, shadowRatio);
			}
		}
		return copyImg;
	}
	
	private int makePixelDarker(int rgb, double ratio) {
		int red = Math.min(255, (int) (getRed(rgb) * ratio));
		int blue = Math.min(255, (int) (getBlue(rgb) * ratio));
		int green = Math.min(255, (int) (getGreen(rgb) * ratio));
		return (red << 16) + (blue << 8) + green;
	}
	
	private int getRed(int rgb) {
		return (rgb >> 16) & 0xFF;
	}
	
	private int getBlue(int rgb) {
		return (rgb >> 8) & 0xFF;
	}
	
	private int getGreen(int rgb) {
		return rgb & 0xFF;
	}
	
	private void setStartingPoint(int x, int y) {
		startX = x;
		startY = y;
	}
	
	private void setEndPoint(int x, int y) {
		endX = x;
		endY = y;
	}
	
}
