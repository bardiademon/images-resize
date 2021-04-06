package com.bardiademon.imagesresize;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;

public final class ImagesResize
{

    public ImagesResize ()
    {
        File image = null;

        final JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled (false);

        final BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));
        final AtomicInteger resultOpenDialog = new AtomicInteger ();

        while (true)
        {
            System.out.print ("Image chooser: ");

            chooser.setCurrentDirectory (image);

            SwingUtilities.invokeLater (() ->
            {
                resultOpenDialog.set (chooser.showOpenDialog (null));
                synchronized (ImagesResize.this)
                {
                    ImagesResize.this.notify ();
                    ImagesResize.this.notifyAll ();
                }
            });

            synchronized (ImagesResize.this)
            {
                try
                {
                    ImagesResize.this.wait ();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace ();
                }
            }

            if (resultOpenDialog.get () == JFileChooser.OPEN_DIALOG)
            {
                image = chooser.getSelectedFile ();
                try
                {
                    final BufferedImage imageRead = ImageIO.read (image);

                    System.out.println (image.getAbsolutePath ());

                    String strWidth, strHeight;
                    int width, height;
                    while (true)
                    {
                        try
                        {
                            System.out.print ("Width: ");
                            strWidth = reader.readLine ();
                            System.out.print ("Height: ");
                            strHeight = reader.readLine ();

                            if (strWidth.equals ("exit") || strHeight.equals ("exit")) System.exit (0);

                            if (strWidth.matches ("[0-9]*") && strHeight.matches ("[0-9]*"))
                            {
                                width = Integer.parseInt (strWidth);
                                height = Integer.parseInt (strHeight);
                                break;
                            }
                            else throw new Exception ("input invalid");
                        }
                        catch (Exception e)
                        {
                            System.out.println ("Reader error or input invalid <" + e.getMessage () + ">");
                        }
                    }
                    final Image ImageReadScaledInstance = imageRead.getScaledInstance (width , height , BufferedImage.TYPE_INT_RGB);
                    final BufferedImage finalImage = new BufferedImage (width , height , BufferedImage.TYPE_INT_RGB);
                    final Graphics2D graphics = finalImage.createGraphics ();
                    graphics.drawImage (ImageReadScaledInstance , 0 , 0 , null);
                    graphics.dispose ();

                    final File output = new File (image.getParent () + File.separator + String.format ("resize(%d.%d)_%s" , width , height , image.getName ()));

                    try
                    {
                        ImageIO.write (finalImage , FilenameUtils.getExtension (output.getName ()) , output);
                        System.out.println ("Successfully <" + output.getAbsolutePath () + ">");
                    }
                    catch (IOException e)
                    {
                        System.out.println ("Error write image <" + e.getMessage () + ">");
                    }

                }
                catch (IOException e)
                {
                    System.out.println ("Error read image <" + e.getMessage () + ">");
                }
            }

            System.out.print ("Again (y/n): ");
            try
            {
                if (reader.readLine ().equals ("n")) System.exit (0);
            }
            catch (IOException e)
            {
                e.printStackTrace ();
            }
        }
    }

    public static void main (String[] args)
    {
        bardiademon.run ();
        new ImagesResize ();
    }
}
