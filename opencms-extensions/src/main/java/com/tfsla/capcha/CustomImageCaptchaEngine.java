package com.tfsla.capcha;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FunkyBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.MultipleShapeBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.fontgenerator.DeformedRandomFontGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.fontgenerator.TwistedRandomFontGenerator;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.image.gimpy.GimpyFactory;

import com.octo.captcha.image.ImageCaptcha;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import java.util.ArrayList;
import java.util.List;

import com.octo.captcha.Captcha;
import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.engine.CaptchaEngineException;
import com.octo.captcha.image.ImageCaptchaFactory;

public class CustomImageCaptchaEngine implements com.octo.captcha.engine.CaptchaEngine

{

	private int width=250;
	private int height=150;
	private String possibleWords="ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
	private String background = "Funky";
	private int red=0;
	private int green=0;
	private int blue=0;
	private int wordLength=7;
	private String fontGeneratorMethod="Random";

	private String siteName = null;
	private String publication = null;

 	protected List<CaptchaFactory> factories = new ArrayList<CaptchaFactory>();
 	protected Random myRandom = new SecureRandom();

	public CustomImageCaptchaEngine(String siteName, String publication)
	{		
		this.siteName = siteName;
		this.publication = publication;
		
		loadProperties();
		
		buildInitialFactories();
		checkFactoriesSize();
	}
	
     private void loadProperties()
     {
    	String module = "captcha";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
 		width = config.getIntegerParam(siteName, publication, module, "width", width);
 		height = config.getIntegerParam(siteName, publication, module, "height", height);

  		possibleWords = config.getParam(siteName, publication, module, "possibleWords", possibleWords);
  		background = config.getParam(siteName, publication, module, "background", background);

  		red = config.getIntegerParam(siteName, publication, module, "red", red);
  		green = config.getIntegerParam(siteName, publication, module, "green", green);
  		blue = config.getIntegerParam(siteName, publication, module, "blue", blue);

  		wordLength = config.getIntegerParam(siteName, publication, module, "wordLength", wordLength);

  		fontGeneratorMethod = config.getParam(siteName, publication, module, "fontGenerator", fontGeneratorMethod);

     }

     protected void buildInitialFactories() {
    	 
    	 if (siteName!=null) {
            WordGenerator wgen = new RandomWordGenerator(possibleWords);
            RandomRangeColorGenerator cgen = new RandomRangeColorGenerator(
                 new int[] {0, 100},
                 new int[] {0, 100},
                 new int[] {0, 100});
            TextPaster textPaster = new RandomTextPaster(new Integer(wordLength), new Integer(wordLength), cgen, true);

            BackgroundGenerator backgroundGenerator;

            if (background.toLowerCase().equals("funky"))
            {
            	backgroundGenerator = new FunkyBackgroundGenerator(new Integer(width), new Integer(height));
            }
            else if (background.toLowerCase().equals("unicolor"))
            {
            	backgroundGenerator = new UniColorBackgroundGenerator(new Integer(width), new Integer(height),new Color(red,green,blue));
            }
            else if (background.toLowerCase().equals("multipleshape"))
            {
            	backgroundGenerator = new MultipleShapeBackgroundGenerator(new Integer(width), new Integer(height));
            }
            else  //default
            {
            	backgroundGenerator = new FunkyBackgroundGenerator(new Integer(width), new Integer(height));
            }

            Font[] fontsList = new Font[] {
                new Font("Arial", 0, 10),
                new Font("Tahoma", 0, 10),
                new Font("Verdana", 0, 10),
             };

            FontGenerator fontGenerator = null;
            if (fontGeneratorMethod.toLowerCase().equals("deformed")) {
            	fontGenerator = new DeformedRandomFontGenerator(new Integer(20), new Integer(35));
            }
            else
            	if (fontGeneratorMethod.toLowerCase().equals("twisted")) {
                	fontGenerator = new TwistedRandomFontGenerator(new Integer(20), new Integer(35));
                }
                else
                	fontGenerator = new RandomFontGenerator(new Integer(20), new Integer(35), fontsList);

            WordToImage wordToImage = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);
             this.addFactory(new GimpyFactory(wgen, wordToImage));
             
    	 }
     }
     
     
     /**
      * Add a factory to the gimpy list
      *
      * @return true if added false otherwise
      */
     public boolean addFactory(
             com.octo.captcha.image.ImageCaptchaFactory factory) {
         return factory != null && this.factories.add(factory);
     }

     /**
      * Add an array of factories to the gimpy list
      */
     public void addFactories(
             ImageCaptchaFactory[] factories) {
         checkNotNullOrEmpty(factories);
         this.factories.addAll(Arrays.asList(factories));
     }

     private void checkFactoriesSize() {
         if (factories.size() == 0)
             throw new CaptchaException(
                     "This gimpy has no factories. Please initialize it "
                             + "properly with the buildInitialFactory() called by "
                             + "the constructor or the addFactory() mehtod later!");
     }


     /**
      * This method build a ImageCaptchaFactory.
      *
      * @return a CaptchaFactory
      */
     public com.octo.captcha.image.ImageCaptchaFactory getImageCaptchaFactory() {
         return (com.octo.captcha.image.ImageCaptchaFactory) factories.get(
                 myRandom.nextInt(factories.size()));
     }
     
     /**
      * This method use an object parameter to build a CaptchaFactory.
      *
      * @return a CaptchaFactory
      */
     public final ImageCaptcha getNextImageCaptcha() {
         return getImageCaptchaFactory().getImageCaptcha();
     }

     /**
      * This return a new captcha. It may be used directly.
      *
      * @param locale the desired locale
      * @return a new Captcha
      */
     public ImageCaptcha getNextImageCaptcha(Locale locale) {
         return getImageCaptchaFactory().getImageCaptcha(locale);
     }

     public final Captcha getNextCaptcha() {
         return getImageCaptchaFactory().getImageCaptcha();
     }

     /**
      * This return a new captcha. It may be used directly.
      *
      * @param locale the desired locale
      * @return a new Captcha
      */
     public Captcha getNextCaptcha(Locale locale) {
         return getImageCaptchaFactory().getImageCaptcha(locale);
     }

     
     /**
      * @return captcha factories used by this engine
      */
     public CaptchaFactory[] getFactories() {
         return this.factories.toArray(new CaptchaFactory[factories.size()]);
     }

     /**
      * @param factories new captcha factories for this engine
      */
     public void setFactories(CaptchaFactory[] factories) throws CaptchaEngineException {
         checkNotNullOrEmpty(factories);
         ArrayList<CaptchaFactory> tempFactories = new ArrayList<CaptchaFactory>();

         for (int i = 0; i < factories.length; i++) {
             if (!ImageCaptchaFactory.class.isAssignableFrom(factories[i].getClass())) {
                 throw new CaptchaEngineException("This factory is not an image captcha factory " + factories[i].getClass());
             }
             tempFactories.add(factories[i]);
         }

         this.factories = tempFactories;
     }

     protected void checkNotNullOrEmpty(CaptchaFactory[] factories) {
         if (factories == null || factories.length == 0) {
             throw new CaptchaEngineException("impossible to set null or empty factories");
         }
     }

}