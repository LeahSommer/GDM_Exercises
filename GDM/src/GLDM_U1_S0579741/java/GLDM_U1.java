package GLDM_U1_S0579741.java;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild",
		"Belgische Fahne",
		"USA Fahne",
		"Schwarz/Weiss Verlauf",
		"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
		"Tschechische Fahne"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1 imageGeneration = new GLDM_U1();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			generateBlackImage(width, height, pixels);
		}

		if ( choice.equals("Gelbes Bild") ) {
			generateYellowImage(width, height, pixels);
		}

		if ( choice.equals("Belgische Fahne") ) {
			generateBelgischImage(width, height, pixels);
		}

		if ( choice.equals("USA Fahne") ) {
			generateUSAImage(width, height, pixels);
		}

		if ( choice.equals("Schwarz/Weiss Verlauf") ) {
			generateSchwarzWeißImage(width, height, pixels);
		}

		if ( choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf") ) {
			generateFarbenImage(width, height, pixels);
		}

		if ( choice.equals("Tschechische Fahne") ) {
			generateTschechischImage(width, height, pixels);
		}


		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateTschechischImage(int width, int height, int[] pixels) {
		int centerX = width / 2;
		int centerY = height / 2;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				boolean isInTriangle = false;
				if (x < centerX) {
					if (y < centerY) {
						// Ist das Y des Punktes oberhalb der oberen Linie des Dreiecks?
						int x1 = 0;
						int y1 = 0;
						int x2 = centerX;
						int y2 = centerY;
						int dx = x2 - x1;
						int dy = y2 - y1;
						int yLine = y1 + dy * (x - x1) / dx;
						if (y > yLine) {
							isInTriangle = true;
						}
					} else {
						// Ist das Y des Punktes unterhalb der unteren Linie des Dreiecks?
						int x1 = 0;
						int y1 = height;
						int x2 = centerX;
						int y2 = centerY;
						int dx = x2 - x1;
						int dy = y2 - y1;
						int yLine = y1 + dy * (x - x1) / dx;
						if (y < yLine) {
							isInTriangle = true;
						}
					}
				}

				int r;
				int g;
				int b;

				if (isInTriangle) {
					// Blau
					r = 0;
					g = 0;
					b = 255;
				} else {
					// Linien
					if (y < centerY) {
						// Weiß
						r = 255;
						g = 255;
						b = 255;
					} else {
						// Rot
						r = 255;
						g = 0;
						b = 0;
					}
				}
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}

	}

	private void generateFarbenImage(int width, int height, int[] pixels) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				double xPercent = (double)(x) / width;
				int r = (int) (xPercent * 255);
				int g = 0;

				double yPercent = (double)(y) / height;
				int b = (int) (yPercent * 255);

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}

	}

	private void generateSchwarzWeißImage(int width, int height, int[] pixels) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				double xPercent = (double)(y) / width;
				int r = (int) (xPercent * 255);
				int g = r;
				int b = r;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateUSAImage(int width, int height, int[] pixels) {
		int stripeHeight = height / 12;
		int blueSegmentHeight = stripeHeight * 6;
		int blueSegmentWidth = (int) (width * 0.5);

		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r;
				int g;
				int b;

				if (x < blueSegmentWidth && y < blueSegmentHeight) {
					r = 0;
					g = 0;
					b = 255;
				} else {
					// Streifen
					int stripeNumber = y / stripeHeight;
					if ((stripeNumber % 2) == 0) {
						// Gerade: Roter Streifen
						r = 255;
						g = 0;
						b = 0;

					} else {
						// Ungerade: Weißer Streifen
						r = 255;
						g = 255;
						b = 255;

					}
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}

	}

	private void generateBelgischImage(int width, int height, int[] pixels) {
		int segmentWidth = width / 3;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r;
				int g;
				int b;

				if (x < segmentWidth) {
					// Schwarz
					r = 0;
					g = 0;
					b = 0;
				} else if (x < 2 * segmentWidth) {
					// Gelb
					r = 255;
					g = 255;
					b = 0;
				} else {
					// Rot
					r = 255;
					g = 0;
					b = 0;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}

	}


	private void generateYellowImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 255;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen
				
				int r = 0;
				int g = 0;
				int b = 0;
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}
	
	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

