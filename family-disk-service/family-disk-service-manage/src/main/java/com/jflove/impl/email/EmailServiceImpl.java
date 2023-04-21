package com.jflove.impl.email;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.email.api.IEmailService;
import com.jflove.email.dto.EmailDetailsDTO;
import com.jflove.mapper.email.EmailSendRecordMapper;
import com.jflove.po.email.EmailSendRecordPO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author tanjun
 * @date 2022/12/9 9:59
 * @describe 邮件发送服务
 */
@DubboService
@Log4j2
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailSendRecordMapper emailSendRecordMapper;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.mail.frequency}")
    private Integer frequency;

    @Override
    @Transactional
    public ResponseHeadDTO<String> sendSimpleMail(EmailDetailsDTO details) {
        try {
            String allow = allowSend(details);
            if(allow != null){
                return new ResponseHeadDTO<String>(false,allow);
            }
            log.debug("发送邮件:{}",details.toString());
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject().getName());
            javaMailSender.send(mailMessage);
            String ret = String.format("邮件主题[%s],发送成功",details.getSubject().getName());
            saveRecord(details);
            return new ResponseHeadDTO<String>(true,ret);
        }catch (Exception e) {
            String err = String.format("邮件主题[%s],发送失败",details.getSubject().getName());
            log.error(err,e);
            return new ResponseHeadDTO<String>(false,err);
        }
    }

    @Override
    @Transactional
    public ResponseHeadDTO<String> sendMailWithAttachment(EmailDetailsDTO details) {
        if(!StringUtils.hasLength(details.getAttachment())){
            return sendSimpleMail(details);
        }
        return new ResponseHeadDTO<String>(false,"暂不支持发送带附件的邮件");
//        // Creating a mime message
//        MimeMessage mimeMessage
//                = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//
//        try {
//
//            // Setting multipart as true for attachments to
//            // be send
//            mimeMessageHelper
//                    = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo(details.getRecipient());
//            mimeMessageHelper.setText(details.getMsgBody());
//            mimeMessageHelper.setSubject(
//                    details.getSubject());
//
//            // Adding the attachment
//            FileSystemResource file
//                    = new FileSystemResource(
//                    new File(details.getAttachment()));
//
//            mimeMessageHelper.addAttachment(
//                    file.getFilename(), file);
//
//            // Sending the mail
//            javaMailSender.send(mimeMessage);
//            return "Mail sent Successfully";
//        }
//
//        // Catch block to handle MessagingException
//        catch (MessagingException e) {
//
//            // Display message when exception occurred
//            return "Error while sending mail!!!";
//        }
    }

    /**
     * 发送成功才记录
     * @param details
     */
    private void saveRecord(EmailDetailsDTO details){
        EmailSendRecordPO recordPO = new EmailSendRecordPO();
        recordPO.setNextSendTime((System.currentTimeMillis() / 1000) + (frequency * 60));
        recordPO.setMsgBody(details.getMsgBody());
        recordPO.setSubjectCode(details.getSubject().getCode());
        recordPO.setSubject(details.getSubject().getName());
        recordPO.setRecipient(details.getRecipient());
        emailSendRecordMapper.insert(recordPO);
    }

    /**
     * 是否允许发送
     * @param details
     * @return 返回 null 代表允许发送
     */
    private String allowSend(EmailDetailsDTO details){
        //判断这个类型的右键是否发送太频繁
        if(emailSendRecordMapper.selectCount(new LambdaQueryWrapper<EmailSendRecordPO>()
                .eq(EmailSendRecordPO::getRecipient,details.getRecipient())
                .eq(EmailSendRecordPO::getSubjectCode,details.getSubject().getCode())
                .gt(EmailSendRecordPO::getNextSendTime,System.currentTimeMillis() / 1000)
        ) > 0){
            return String.format("发送太频繁了,请%s分钟后再试试",frequency);
        }
        return null;
    }
}
