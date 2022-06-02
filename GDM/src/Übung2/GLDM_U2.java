package Übung2;

import java.math.*;

import ij.IJ;
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

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static java.lang.Math.toRadians;

/**
     Opens an image window and adds a panel below the image
*/
public class GLDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
    	IJ.open("C:\\Users\\somme\\Downloads\\orchid.jpg");
		
		GLDM_U2 pw = new GLDM_U2();
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
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderKontrast;
		private JSlider jSliderFarbsaettigung;
		private JSlider jSliderHue;
		private double hue = 0;
		private double saettigung = 1;
		private double brightness = 0;
		private double kontrast = 1 ;


		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", -150, 350, 100);
            jSliderKontrast = makeTitledSilder("Kontrast", 0, 10, 1);
			jSliderFarbsaettigung = makeTitledSilder("Sättigung", 0, 5,1);
			jSliderHue = makeTitledSilder("Hue", 0, 360, 90);
			panel.add(jSliderHue);
			panel.add(jSliderFarbsaettigung);
            panel.add(jSliderBrightness);
            panel.add(jSliderKontrast);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-100;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderKontrast) {
				kontrast= slider.getValue();
				String str = "Kontrast" + kontrast;
				setSliderTitle(jSliderKontrast, str);
			}

			if (slider == jSliderFarbsaettigung) {
				saettigung = slider.getValue();
				String str = "Sättigung " + saettigung;
				setSliderTitle(jSliderFarbsaettigung, str);
			}
			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue " + hue;
				setSliderTitle(jSliderHue, str);
			}


			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;


					//RGB Farben in YUV umgewandelt
					double yColor = 0.299 * r + 0.587 * g + 0.114 * b;
					double uColor = (b - yColor) * 0.493;
					double vColor = (r - yColor) * 0.877;

					//Helligkeit
					 double yColorC = yColor + brightness;

					 //Kontrast
					 double yColorH = yColor * kontrast;


					 //Sättigung
					uColor = uColor * saettigung;
					vColor = vColor * saettigung;

					//Hue
					double hue2 = Math.toRadians(hue);
					uColor = uColor * Math.cos(hue2) - vColor * Math.sin(hue2);
					vColor = uColor * Math.sin(hue2) + vColor * Math.cos(hue2);


					yColor = yColorC + yColorH;


					//YUV Farben in RGB umgewandelt
					int rn = (int) (yColor + vColor/0.877);
					int bn = (int) (yColor + uColor/0.493);
					int gn = (int) (1/0.587 * yColor - 0.299/0.587*rn - 0.114/0.587 * bn);


					// anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
					// die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren
					//int rn = (int) (r + brightness);
					if(rn >=255 ){
						rn = 255;
					}else if (rn <= 0){
						rn = 0;
					}

					//int gn = (int) (g + brightness);
					if(gn >=255 ){
						gn = 255;
					}else if (gn <= 0){
						gn = 0;
					}

					//int bn = (int) (b + brightness);
					if(bn >=255 ){
						bn = 255;
					}else if (bn <= 0){
						bn = 0;
					}

					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;

				}
			}
		}
		
    } // CustomWindow inner class
} 


