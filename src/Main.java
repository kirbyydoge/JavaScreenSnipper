package src;
import java.awt.Window;

public class Main {

	private static final int IDX_PATH = 0;
	private static final int IDX_SHADOW = 1;

	public static void main(String[] args) {
		if(args.length == 2) {
			new ScreenSnipper(new Window(null), args[IDX_PATH], Double.parseDouble(args[IDX_SHADOW]));
		}
		else {
			new ScreenSnipper(new Window(null), "./", 0.6);
		}
	}
	
}
