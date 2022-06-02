package Übung5;

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
public class GLDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Filter 1", "Weichzeichner", "Hochpassfilter", "Scharfe Kanten"};


	public static void main(String args[]) {

		IJ.open("C:/Users/somme/Documents/sail.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GLDM_U5 pw = new GLDM_U5();
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


			// with help from http://www.mmi.ifi.lmu.de/fileadmin/mimuc/mt_ss05/mtB3.pdf

			if (method.equals("Weichzeichner")) {

				float factor = 1/9f;

				// loop over x, y pixels
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y * width+x;

						float r = 0;
						float g = 0;
						float b = 0;


						// loop over pixel
						for (int m=-1; m<2; m++) {
							for (int n=-1; n<2; n++) {


								int argb = getPixel(x+n, y+m);
								r += factor * ((argb >> 16) & 0xff);
								g += factor * ((argb >> 8) & 0xff);
								b += factor * (argb & 0xff);
							}
						}

						int rn = (int) r;
						int gn = (int) g;
						int bn = (int) b;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;



					}
				}
			}

			if (method.equals("Hochpassfilter")) {


				float factor = -1/9f;

				float middle = 1-factor;

				// loop over x, y pixels
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y * width+x;

						float r = 0;
						float g = 0;
						float b = 0;

						// loop over kernel pixel
						for (int m=-1; m<2; m++) {
							for (int n=-1; n<2; n++) {



								int argb = getPixel(x+n, y+m);
								if (m==0 && n==0) {

									// only for pixels in the middle
									r += middle * ((argb >> 16) & 0xff);
									g += middle * ((argb >> 8) & 0xff);
									b += middle * (argb & 0xff);
								}

								else {

									// all other pixels
									r += factor * ((argb >> 16) & 0xff);
									g += factor * ((argb >> 8) & 0xff);
									b += factor * (argb & 0xff);
								}
							}
						}

						int rn = (int) r+128;
						int gn = (int) g+128;
						int bn = (int) b+128;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;

					}
				}
			}

			if (method.equals("Scharfe Kanten")) {

				float factor = -1/9f;

				float middle = 2-factor;

				// loop over x, y pixels
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y * width+x;

						float r = 0;
						float g = 0;
						float b = 0;

						// loop over pixel
						for (int m=-1; m<2; m++) {
							for (int n=-1; n<2; n++) {

								int argb = getPixel(x+n, y+m);

								if (m==0 && n==0) {

									// only for pixels in the
									r += middle * ((argb >> 16) & 0xff);
									g += middle * ((argb >> 8) & 0xff);
									b += middle * (argb & 0xff);
								}

								else {

									// all other pixels
									r += factor * ((argb >> 16) & 0xff);
									g += factor * ((argb >> 8) & 0xff);
									b += factor * (argb & 0xff);
								}
							}
						}

						int rn = (int) Limit(r);
						int gn = (int) Limit(g);
						int bn = (int) Limit(b);

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
			}


			
			if (method.equals("Filter 1")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = r/2;
						int gn = g/2;
						int bn = b/2;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
			}


		}

		//Natalia Marth helped with the limit and get pixel method
		public float Limit(float pixel) {

			if (pixel<0) return 0;
			else if (pixel>255) return 255;
			else return pixel;
		}

		public int getPixel(int x, int y)
		{
			if (y<0)
			{
				y = 0;
			}else if(y>=height)
			{
				y=height-1;

			}
			if (x<0)
			{
				x=0;
			}else if(x>=width)
			{
				x=width-1;
			}

			return origPixels[y*width + x];
		}
	}


} // CustomWindow inner class

