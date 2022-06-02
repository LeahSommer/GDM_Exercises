package Übung4;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GLDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Wisch-Blende","Weiche Blende","Overlay", "Schieb-Blende", "Chroma Key", "Extra"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		IJ.open("C:\\Users\\somme\\Downloads\\StackB.zip");
		
		GLDM_U4 sd = new GLDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Wisch-Blende")) methode =2 ;
		if (s.equals("Weiche Blende")) methode = 3;
		if (s.equals("Overlay")) methode = 4;
		if (s.equals("Schieb-Blende")) methode = 5;
		if (s.equals("Chroma Key")) methode = 6;
		if (s.equals("Extra")) methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++, pos++)
				{
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					if (methode == 1)
					{
					if (x+1 > (z-1)*(double)width/(length-1))
						pixels_Erg[pos] = pixels_B[pos];
					else
						pixels_Erg[pos] = pixels_A[pos];
					}

                    //Wisch-Blende
					if (methode == 2)
					{
						if (y+1 > (z-1)*(double)width/(length-1))
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];

					}
					//weichblende
					if (methode == 3)
					{
						double transparenz = 255 / length * z;
						double differenz = 255 - transparenz;
						int r = (int) ((transparenz * rA + differenz * rB) / 255);
						int g = (int) ((transparenz * gA + differenz * gB) / 255);
						int b = (int) ((transparenz * bA + differenz * bB) / 255);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					//Overlay
					if (methode == 4)
					{
						if (rB <= 128)
							 rB = rA * rB / 128;
							else
								 rB = 255 - ((255 - rA) * (255 - rB) / 128);

						if (gB <= 128)
							 gB = gA * gB / 128;
						else
							 gB = 255 - ((255 - gA) * (255 - gB) / 128);

						if (bB <= 128)
							 bB = bA * bB / 128;
						else
							 bB = 255 - ((255 - bA) * (255 - bB) / 128);


						pixels_Erg[pos] = 0xFF000000 + ((rB & 0xff) << 16) + ((gB & 0xff) << 8) + ( bB & 0xff);
					}
					//Schieb-Blende - Diese Aufgabe wurde mit Hilfe von Alex Erenhöfer bearbeitet.
					if (methode == 5)
					{
						int a = width - x;
						int a2 = width / 100 * z;
						if( pos + a2 < pixels_Erg.length) pixels_Erg[pos + a2] = pixels_A[pos];
						if (a2 < a){
							pixels_Erg[pos + a2] = pixels_B[pos];
						} else {
							pixels_Erg[pos + (a - a2)] = pixels_B[pos];
						}

					}
                    //chroma keying mit Hilfe des Color Inspektors
					if (methode == 6)
					{
						double distance = Math.sqrt((225 - rA) * (225 - rA) + (170 - gA ) * (170 - gA) + (60 - bA) * (60 - bA));
						if (distance < 100) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							pixels_Erg[pos] = pixels_A[pos];
						}
					}
					// Ich wollte, dass das 2. video von rechts oben durchläuft. Das habe ich aber nicht perfekt geschafft.
					if (methode == 7)
					{
							if ((x < z * (((width / 5) * 20) / length))
									&& (x > z * (width / 5) / length)
									&& (y < z * ((height / 5) * 20) / length)
									&& (y > z * (height / 5) / length)) {
								pixels_Erg[pos] = pixels_A[pos];
							} else {
								pixels_Erg[pos] = pixels_B[pos];
							}
					}
				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

}

