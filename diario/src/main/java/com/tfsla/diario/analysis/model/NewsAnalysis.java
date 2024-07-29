package com.tfsla.diario.analysis.model;

public class NewsAnalysis {
	private int bodyNumberParagrapths;
	private int bodyMeanParagrapthsLength;
	private int bodyMeanParagrapthsWordsCount;
	private int bodyLargestParagrapthLength;
	private int bodyLargestParagrapthWords;
	private int bodyTextLength;
	private int bodyWordCount;

	private int titleWordCount;
	private int titleTextLength;

	private int keyWordsLength;
	private int peopleLength;

	private int embeddedFacebook;
	private int embeddedFlick;
	private int embeddedInstagram;
	private int embeddedPinteres;
	private int embeddedStorify;
	private int embeddedTwitter;
	private int embeddedVine;
	private int embeddedYoutube;

	private int embeddedImage;
	private int embeddedImageGalleries;
	private int embeddedImageComparator;

	private int embeddedVideo;
	private int embeddedVideoGalleries;

	private int embeddedAudio;
	private int embeddedAudioGalleries;

	private int embeddedPoll;
	
	private int subTitleMeanParagrapthsWordsCount;
	private int subTitleTextLength;
	private int subTitleWordCount;
	private int subTitleNumberParagrapths;
	private int subTitleLargestParagrapthLength;
	private int subTitleLargestParagrapthWords;
	private int subTitleMeanParagrapthsLength;
	private int imagePreviewCount;
	private int imagePreviewTitleTextLength;
	private int imagePreviewTitleWordCount;
	private int imagePreviewKeyWordsLength;
	private int imageGalleriesCount;
	private boolean imageGalleriesWithNoDescription;
	private boolean imageGalleriesWithNoKeywords;
	
	private int videoYouTubeCount;
	private boolean videoYouTubeWithNoTitle;
	private boolean videoYouTubeWithNoKeywords;
	private boolean videoYouTubeWithNoImage;
	
	private int videoEmbeddedCount;
	private boolean videoEmbeddedWithNoTitle;
	private boolean videoEmbeddedWithNoImage;
	private boolean videoEmbeddedWithNoKeywords;
	
	private int videoFlashCount;
	private boolean videoFlashWithNoTitle;
	private boolean videoFlashWithNoImage;
	private boolean videoFlashWithNoKeywords;

	public int getBodyNumberParagrapths() {
		return bodyNumberParagrapths;
	}

	public void setBodyNumberParagrapths(int bodyNumberParagrapths) {
		this.bodyNumberParagrapths = bodyNumberParagrapths;
	}

	public int getBodyMeanParagrapthsLength() {
		return bodyMeanParagrapthsLength;
	}

	public void setBodyMeanParagrapthsLength(int bodyMeanParagrapthsLength) {
		this.bodyMeanParagrapthsLength = bodyMeanParagrapthsLength;
	}

	public int getBodyTextLength() {
		return bodyTextLength;
	}

	public void setBodyTextLength(int bodyTextLength) {
		this.bodyTextLength = bodyTextLength;
	}

	public int getBodyWordCount() {
		return bodyWordCount;
	}

	public void setBodyWordCount(int bodyWordCount) {
		this.bodyWordCount = bodyWordCount;
	}

	public int getTitleWordCount() {
		return titleWordCount;
	}

	public void setTitleWordCount(int titleWordCount) {
		this.titleWordCount = titleWordCount;
	}

	public int getTitleTextLength() {
		return titleTextLength;
	}

	public void setTitleTextLength(int titleTextLength) {
		this.titleTextLength = titleTextLength;
	}

	public int getEmbeddedFacebook() {
		return embeddedFacebook;
	}

	public void setEmbeddedFacebook(int embeddedFacebook) {
		this.embeddedFacebook = embeddedFacebook;
	}

	public int getEmbeddedFlick() {
		return embeddedFlick;
	}

	public void setEmbeddedFlick(int embeddedFlick) {
		this.embeddedFlick = embeddedFlick;
	}

	public int getEmbeddedInstagram() {
		return embeddedInstagram;
	}

	public void setEmbeddedInstagram(int embeddedInstagram) {
		this.embeddedInstagram = embeddedInstagram;
	}

	public int getEmbeddedPinteres() {
		return embeddedPinteres;
	}

	public void setEmbeddedPinteres(int embeddedPinteres) {
		this.embeddedPinteres = embeddedPinteres;
	}

	public int getEmbeddedStorify() {
		return embeddedStorify;
	}

	public void setEmbeddedStorify(int embeddedStorify) {
		this.embeddedStorify = embeddedStorify;
	}

	public int getEmbeddedTwitter() {
		return embeddedTwitter;
	}

	public void setEmbeddedTwitter(int embeddedTwitter) {
		this.embeddedTwitter = embeddedTwitter;
	}

	public int getEmbeddedVine() {
		return embeddedVine;
	}

	public void setEmbeddedVine(int embeddedVine) {
		this.embeddedVine = embeddedVine;
	}

	public int getEmbeddedYoutube() {
		return embeddedYoutube;
	}

	public void setEmbeddedYoutube(int embeddedYoutube) {
		this.embeddedYoutube = embeddedYoutube;
	}

	public int getEmbeddedImage() {
		return embeddedImage;
	}

	public void setEmbeddedImage(int embeddedImage) {
		this.embeddedImage = embeddedImage;
	}

	public int getEmbeddedImageGalleries() {
		return embeddedImageGalleries;
	}

	public void setEmbeddedImageGalleries(int embeddedImageGalleries) {
		this.embeddedImageGalleries = embeddedImageGalleries;
	}

	public int getEmbeddedImageComparator() {
		return embeddedImageComparator;
	}

	public void setEmbeddedImageComparator(int embeddedImageComparator) {
		this.embeddedImageComparator = embeddedImageComparator;
	}

	public int getEmbeddedVideo() {
		return embeddedVideo;
	}

	public void setEmbeddedVideo(int embeddedVideo) {
		this.embeddedVideo = embeddedVideo;
	}

	public int getEmbeddedVideoGalleries() {
		return embeddedVideoGalleries;
	}

	public void setEmbeddedVideoGalleries(int embeddedVideoGalleries) {
		this.embeddedVideoGalleries = embeddedVideoGalleries;
	}

	public int getEmbeddedAudio() {
		return embeddedAudio;
	}

	public void setEmbeddedAudio(int embeddedAudio) {
		this.embeddedAudio = embeddedAudio;
	}

	public int getEmbeddedAudioGalleries() {
		return embeddedAudioGalleries;
	}

	public void setEmbeddedAudioGalleries(int embeddedAudioGalleries) {
		this.embeddedAudioGalleries = embeddedAudioGalleries;
	}

	public int getEmbeddedPoll() {
		return embeddedPoll;
	}

	public void setEmbeddedPoll(int embeddedPoll) {
		this.embeddedPoll = embeddedPoll;
	}

	public int getBodyMeanParagrapthsWordsCount() {
		return bodyMeanParagrapthsWordsCount;
	}

	public void setBodyMeanParagrapthsWordsCount(int bodyMeanParagrapthsWordsCount) {
		this.bodyMeanParagrapthsWordsCount = bodyMeanParagrapthsWordsCount;
	}

	public void setBodyLargestParagrapthLength(int bodyLargestParagrapthLength) {
		this.bodyLargestParagrapthLength = bodyLargestParagrapthLength;

	}

	public void setBodyLargestParagrapthWords(int bodyLargestParagrapthWords) {
		this.bodyLargestParagrapthWords = bodyLargestParagrapthWords;

	}

	public int getBodyLargestParagrapthLength() {
		return bodyLargestParagrapthLength;
	}

	public int getBodyLargestParagrapthWords() {
		return bodyLargestParagrapthWords;
	}

	public int getKeyWordsLength() {
		return keyWordsLength;
	}

	public void setKeyWordsLength(int keyWordsLength) {
		this.keyWordsLength = keyWordsLength;
	}

	public int getPeopleLength() {
		return peopleLength;
	}

	public void setPeopleLength(int peopleLength) {
		this.peopleLength = peopleLength;
	}

	public void setSubTitleTextLength(int subTitleTextLength) {
		this.subTitleTextLength = subTitleTextLength;
	}

	public void setSubTitleWordCount(int subTitleWordCount) {
		this.subTitleWordCount = subTitleWordCount;
	}

	public void setSubTitleNumberParagrapths(int subTitleNumberParagrapths) {
		this.subTitleNumberParagrapths = subTitleNumberParagrapths;
	}

	public void setSubTitleLargestParagrapthLength(int subTitleLargestParagrapthLength) {
		this.subTitleLargestParagrapthLength = subTitleLargestParagrapthLength;
	}

	public void setSubTitleLargestParagrapthWords(int subTitleLargestParagrapthWords) {
		this.subTitleLargestParagrapthWords = subTitleLargestParagrapthWords;
	}

	public void setSubTitleMeanParagrapthsLength(int subTitleMeanParagrapthsLength) {
		this.subTitleMeanParagrapthsLength = subTitleMeanParagrapthsLength;
		
	}

	public void setSubTitleMeanParagrapthsWordsCount(int subTitleMeanParagrapthsWordsCount) {
		this.subTitleMeanParagrapthsWordsCount = subTitleMeanParagrapthsWordsCount;		
	}

	public int getSubTitleMeanParagrapthsWordsCount() {
		return subTitleMeanParagrapthsWordsCount;
	}

	public int getSubTitleTextLength() {
		return subTitleTextLength;
	}

	public int getSubTitleWordCount() {
		return subTitleWordCount;
	}

	public int getSubTitleNumberParagrapths() {
		return subTitleNumberParagrapths;
	}

	public int getSubTitleLargestParagrapthLength() {
		return subTitleLargestParagrapthLength;
	}

	public int getSubTitleLargestParagrapthWords() {
		return subTitleLargestParagrapthWords;
	}

	public int getSubTitleMeanParagrapthsLength() {
		return subTitleMeanParagrapthsLength;
	}

	public void setImagePreviewCount(int imagePreviewCount) {
		this.imagePreviewCount = imagePreviewCount;
	}

	public void setImagePreviewTitleTextLength(int imagePreviewTitleTextLength) {
		this.imagePreviewTitleTextLength = imagePreviewTitleTextLength;	
	}

	public void setImagePreviewTitleWordCount(int imagePreviewTitleWordCount) {
		this.imagePreviewTitleWordCount = imagePreviewTitleWordCount;
	}

	public void setImagePreviewKeyWordsLength(int imagePreviewKeyWordsLength) {
		this.imagePreviewKeyWordsLength = imagePreviewKeyWordsLength;
	}

	public int getImagePreviewCount() {
		return imagePreviewCount;
	}

	public int getImagePreviewTitleTextLength() {
		return imagePreviewTitleTextLength;
	}

	public int getImagePreviewTitleWordCount() {
		return imagePreviewTitleWordCount;
	}

	public int getImagePreviewKeyWordsLength() {
		return imagePreviewKeyWordsLength;
	}

	public void setImageGalleriesCount(int imageGalleriesCount) {
		this.imageGalleriesCount = imageGalleriesCount;
	}

	public void setImageGalleriesWithNoDescription(boolean imageGalleriesWithNoDescription) {
		this.imageGalleriesWithNoDescription = imageGalleriesWithNoDescription;
	}

	public void setImageGalleriesWithNoKeywords(boolean imageGalleriesWithNoKeywords) {
		this.imageGalleriesWithNoKeywords = imageGalleriesWithNoKeywords;
		
	}

	public int getImageGalleriesCount() {
		return imageGalleriesCount;
	}

	public boolean isImageGalleriesWithNoDescription() {
		return imageGalleriesWithNoDescription;
	}

	public boolean isImageGalleriesWithNoKeywords() {
		return imageGalleriesWithNoKeywords;
	}

	public void setVideoYouTubeCount(int videoYouTubeCount) {
		this.videoYouTubeCount = videoYouTubeCount;
	}

	public void setVideoYouTubeWithNoTitle(boolean videoYouTubeWithNoTitle) {
		this.videoYouTubeWithNoTitle = videoYouTubeWithNoTitle;
	}

	public void setVideoYouTubeWithNoKeywords(boolean videoYouTubeWithNoKeywords) {
		this.videoYouTubeWithNoKeywords = videoYouTubeWithNoKeywords;		
	}

	public void setVideoYouTubeWithNoImage(boolean videoYouTubeWithNoImage) {
		this.videoYouTubeWithNoImage = videoYouTubeWithNoImage;
	}

	public int getVideoYouTubeCount() {
		return videoYouTubeCount;
	}

	public boolean isVideoYouTubeWithNoTitle() {
		return videoYouTubeWithNoTitle;
	}

	public boolean isVideoYouTubeWithNoKeywords() {
		return videoYouTubeWithNoKeywords;
	}

	public boolean isVideoYouTubeWithNoImage() {
		return videoYouTubeWithNoImage;
	}

	public void setVideoEmbeddedCount(int videoEmbeddedCount) {
		this.videoEmbeddedCount = videoEmbeddedCount;
	}

	public void setVideoEmbeddedWithNoTitle(boolean videoEmbeddedWithNoTitle) {
		this.videoEmbeddedWithNoTitle = videoEmbeddedWithNoTitle;
	}

	public void setVideoEmbeddedWithNoImage(boolean videoEmbeddedWithNoImage) {
		this.videoEmbeddedWithNoImage = videoEmbeddedWithNoImage;
	}

	public void setVideoEmbeddedWithNoKeywords(boolean videoEmbeddedWithNoKeywords) {
		this.videoEmbeddedWithNoKeywords = videoEmbeddedWithNoKeywords;
	}

	public int getVideoEmbeddedCount() {
		return videoEmbeddedCount;
	}

	public boolean isVideoEmbeddedWithNoTitle() {
		return videoEmbeddedWithNoTitle;
	}

	public boolean isVideoEmbeddedWithNoImage() {
		return videoEmbeddedWithNoImage;
	}

	public boolean isVideoEmbeddedWithNoKeywords() {
		return videoEmbeddedWithNoKeywords;
	}

	public int getVideoFlashCount() {
		return videoFlashCount;
	}

	public void setVideoFlashCount(int videoFlashCount) {
		this.videoFlashCount = videoFlashCount;
	}

	public boolean isVideoFlashWithNoTitle() {
		return videoFlashWithNoTitle;
	}

	public void setVideoFlashWithNoTitle(boolean videoFlashWithNoTitle) {
		this.videoFlashWithNoTitle = videoFlashWithNoTitle;
	}

	public boolean isVideoFlashWithNoImage() {
		return videoFlashWithNoImage;
	}

	public void setVideoFlashWithNoImage(boolean videoFlashWithNoImage) {
		this.videoFlashWithNoImage = videoFlashWithNoImage;
	}

	public boolean isVideoFlashWithNoKeywords() {
		return videoFlashWithNoKeywords;
	}

	public void setVideoFlashWithNoKeywords(boolean videoFlashWithNoKeywords) {
		this.videoFlashWithNoKeywords = videoFlashWithNoKeywords;
	}

	@Override
	public String toString() {
		return "NewsAnalysis [bodyNumberParagrapths=" + bodyNumberParagrapths + ", bodyMeanParagrapthsLength="
				+ bodyMeanParagrapthsLength + ", bodyMeanParagrapthsWordsCount=" + bodyMeanParagrapthsWordsCount
				+ ", bodyLargestParagrapthLength=" + bodyLargestParagrapthLength + ", bodyLargestParagrapthWords="
				+ bodyLargestParagrapthWords + ", bodyTextLength=" + bodyTextLength + ", bodyWordCount=" + bodyWordCount
				+ ", titleWordCount=" + titleWordCount + ", titleTextLength=" + titleTextLength + ", keyWordsLength="
				+ keyWordsLength + ", peopleLength=" + peopleLength + ", embeddedFacebook=" + embeddedFacebook
				+ ", embeddedFlick=" + embeddedFlick + ", embeddedInstagram=" + embeddedInstagram
				+ ", embeddedPinteres=" + embeddedPinteres + ", embeddedStorify=" + embeddedStorify
				+ ", embeddedTwitter=" + embeddedTwitter + ", embeddedVine=" + embeddedVine + ", embeddedYoutube="
				+ embeddedYoutube + ", embeddedImage=" + embeddedImage + ", embeddedImageGalleries="
				+ embeddedImageGalleries + ", embeddedImageComparator=" + embeddedImageComparator + ", embeddedVideo="
				+ embeddedVideo + ", embeddedVideoGalleries=" + embeddedVideoGalleries + ", embeddedAudio="
				+ embeddedAudio + ", embeddedAudioGalleries=" + embeddedAudioGalleries + ", embeddedPoll="
				+ embeddedPoll + ", subTitleMeanParagrapthsWordsCount=" + subTitleMeanParagrapthsWordsCount
				+ ", subTitleTextLength=" + subTitleTextLength + ", subTitleWordCount=" + subTitleWordCount
				+ ", subTitleNumberParagrapths=" + subTitleNumberParagrapths + ", subTitleLargestParagrapthLength="
				+ subTitleLargestParagrapthLength + ", subTitleLargestParagrapthWords=" + subTitleLargestParagrapthWords
				+ ", subTitleMeanParagrapthsLength=" + subTitleMeanParagrapthsLength + ", imagePreviewCount="
				+ imagePreviewCount + ", imagePreviewTitleTextLength=" + imagePreviewTitleTextLength
				+ ", imagePreviewTitleWordCount=" + imagePreviewTitleWordCount + ", imagePreviewKeyWordsLength="
				+ imagePreviewKeyWordsLength + ", imageGalleriesCount=" + imageGalleriesCount
				+ ", imageGalleriesWithNoDescription=" + imageGalleriesWithNoDescription
				+ ", imageGalleriesWithNoKeywords=" + imageGalleriesWithNoKeywords + ", videoYouTubeCount="
				+ videoYouTubeCount + ", videoYouTubeWithNoTitle=" + videoYouTubeWithNoTitle
				+ ", videoYouTubeWithNoKeywords=" + videoYouTubeWithNoKeywords + ", videoYouTubeWithNoImage="
				+ videoYouTubeWithNoImage + ", videoEmbeddedCount=" + videoEmbeddedCount + ", videoEmbeddedWithNoTitle="
				+ videoEmbeddedWithNoTitle + ", videoEmbeddedWithNoImage=" + videoEmbeddedWithNoImage
				+ ", videoEmbeddedWithNoKeywords=" + videoEmbeddedWithNoKeywords + ", videoFlashCount="
				+ videoFlashCount + ", videoFlashWithNoTitle=" + videoFlashWithNoTitle + ", videoFlashWithNoImage="
				+ videoFlashWithNoImage + ", videoFlashWithNoKeywords=" + videoFlashWithNoKeywords + "]";
	}


}
