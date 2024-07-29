package com.tfsla.opencms.mail;

import org.opencms.file.CmsUser;

public class MailSender {

	public static final String senderCode = "7526op1cboy1p28VdfvHtUCL7Rr";
	private static MailSender instance = new MailSender();

	public static MailSender getInstance() {
		return instance;
	}

	private MailSender() {
	}

	public void sendMail(final CmsUser cmsUser, final SimpleMail mail) {
		final String userName = cmsUser.getName();
		final String destinationMail = cmsUser.getEmail();

		this.sendMail(mail, userName, destinationMail);
	}

	public void sendMail(final SimpleMail mail, final String userName, final String destinationMail) {
//		Thread mailSender = new Thread() {
//			@Override
//			public void run() {
//				try {

					mail.send();
//				}
//				catch (Exception e) {
//					LogUtils.secureErrorLog(this.getClass(), "Error al enviar mail [to: " + userName + "("
//							+ destinationMail + ")" + "]", e);
//				}
//			}
//		};

		// enviamos el mail en otro thread para no bloquearnos
//		mailSender.start();
	}
}