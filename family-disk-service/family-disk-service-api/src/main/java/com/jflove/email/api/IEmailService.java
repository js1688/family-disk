package com.jflove.email.api;

import com.jflove.email.dto.EmailDetailsDTO;

/**
 * @author tanjun
 * @date 2022/12/9 10:00
 * @describe 邮件发送
 */
public interface IEmailService {
    /**
     * 此方法可用于向所需收件人发送简单的文本电子邮件
     * @param details
     * @return
     */
    String sendSimpleMail(EmailDetailsDTO details);

    /**
     * 此方法可用于将电子邮件连同附件一起发送给所需的收件人
     * @param details
     * @return
     */
    String sendMailWithAttachment(EmailDetailsDTO details);
}
