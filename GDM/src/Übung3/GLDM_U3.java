package Übung3;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GLDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Rot-Kanal", "Graustufen", "Negativ", "Binärbild", "Binäre Fehlerdiffusion", "Sepia", "8 - Farben"};


	public static void main(String args[]) {

		IJ.open("C:\\Users\\somme\\Downloads\\bear.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GLDM_U3 pw = new GLDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Rot-Kanal")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("Negativ")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						double yColor = 0.300 * r + 0.590 * g + 0.120 * b;
						double uColor = (b - yColor) * 0.500;
						double vColor = (r - yColor) * 0.880;

						vColor = vColor * 0;
						uColor = uColor * 0;

						int rn = (int) (yColor + vColor / 0.890);
						int bn = (int) (yColor + uColor / 0.500);
						int gn = (int) (1 / 0.590 * yColor - 0.300 / 0.590 * rn - 0.120 / 0.590 * bn);

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("Binärbild")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						if (r + g + b > 383){
							r = 255;
							g = 255;
							b = 255;
						} else {
							r = 0;
							g = 0;
							b = 0;
						}

						int rn = r;
						int bn = g;
						int gn = b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("Binäre Fehlerdiffusion")) {
				int error = 0;


				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = 255;
						int gn = 255;
						int bn = 255;

						if ((r + g + b + error) < 380){
							rn = 0;
							gn = 0;
							bn = 0;
						}
						error = ((r + g + b + error) - (rn + gn + bn));


						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("Sepia")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						double yColor = 0.300 * r + 0.590 * g + 0.120 * b;
						double uColor = (b - yColor) * 0.500;
						double vColor = (r - yColor) * 0.880;

						vColor = vColor * 0;
						uColor = uColor * 0;

						 r = (int) (yColor + vColor / 0.890);
						 b = (int) (yColor + uColor / 0.500);
						 g = (int) (1 / 0.590 * yColor - 0.300 / 0.590 * r - 0.120 / 0.590 * b);

						int rn = r + 80;
						int gn = g + 60;
						int bn = b + 40;

						if (rn >= 255 ){
							rn = 255;
						}
						if (gn >= 255 ){
							gn = 255;
						}
						if (bn >= 255 ){
							bn = 255;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			if (method.equals("8 - Farben")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = 0;
						int gn = 0;
						int bn = 0;

						if (r < 40){
							rn = 30;
							gn = 35;
							bn = 33;
						} else if (((r > 40 && r <= 60) && g > 80) || (( r > 60 && r <= 100) && b > 100 )) {
							rn = 50;
							gn = 105;
							bn = 140;
						} else if (( r > 40 && r <= 60) && g < 80) {
							rn = 30;
							gn = 28;
							bn = 32;
						}  else if ( r > 60 && r <= 95) {
							rn = 80;
							gn = 70;
							bn = 65;
						} else if ( r > 95 && r <= 130) {
							rn = 115;
							gn = 105;
							bn = 90;
						} else if ( r > 130 && r <= 170) {
							rn = 135;
							gn = 120;
							bn = 110;
						} else if ( r > 170 && r <= 180) {
							rn = 150;
							gn = 150;
							bn = 150;
						} else if ( r > 180 && r <= 220) {
							rn = 240;
							gn = 235;
							bn = 220;
						} else if ( r > 220 && r <= 255) {
							rn = 255;
							gn = 255;
							bn = 255;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			
		}


	} // CustomWindow inner class
} 
