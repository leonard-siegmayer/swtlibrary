package de.teamA.SWT.service.email;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.EmailEntry;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.Wish;
import de.teamA.SWT.repository.EmailRepository;
import de.teamA.SWT.repository.MediumRepository;
import de.teamA.SWT.util.DateUtil;

@Service
public class EmailService {

    private final MediumRepository mediumRepository;
    private final EmailRepository emailRepository;
    // Keys for the metaInfo column the emailEntry database
    private final String metaInfoBorrowingKey = "borrowingIds";
    private final String metaInfoWishesKey = "wishes";
    Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private JavaMailSender sender;
    @Value("${spring.mail.username}")
    private String systemAddress;
    @Value("${mail.swtlib.admin.email}")
    private String adminAddress;
    @Value("${mail.swtlib.university.library.email}")
    private String universityLibraryAddress;
    @Value("${mail.swtlib.signature}")
    private String defaultSignature;
    @Value("${mail.db.maxage}")
    private Integer dbMaxAge;
    private TemplateManager templateManager;
    @Value("${mail.switlib.wishlist.updateMail}")
    private String wishlistEmail;

    @Autowired
    public EmailService(MediumRepository mediumRepository, EmailRepository emailRepository) {
        this.mediumRepository = mediumRepository;
        this.emailRepository = emailRepository;

        this.templateManager = new TemplateManager();
    }

    @PostConstruct
    private void init() {
        templateManager.init(defaultSignature, adminAddress);
    }

    public void cleanupDatabase() {
        LocalDate cleanupDate = LocalDate.now().minusDays(dbMaxAge);
        emailRepository.deleteByDateBefore(DateUtil.toDate(cleanupDate));
    }

    private void sendMail(String mailType, EmailTemplate template, Map<String, List<String>> metaInfo)
            throws EmailServiceException {

        SimpleMailMessage javaMail = getEmptyMail();

        if (template.getRecipient() == null || template.getRecipient().isEmpty()) {
            throw new EmailServiceException("Recipient is null or empty. Won't send mail.");
        }

        javaMail.setTo(template.getRecipient());

        if (template.getCc() != null && template.getCc().size() > 0) {
            javaMail.setCc((String[]) template.getCc().toArray());
        }

        try {

            Email generatedEmail = template.compile();

            javaMail.setSubject(generatedEmail.getSubject());
            javaMail.setText(generatedEmail.getBody());

            sender.send(javaMail);
        } catch (TemplateException | MailException e) {
            throw new EmailServiceException(e);
        }

        emailRepository.save(new EmailEntry(template.recipient, template.getCc(), mailType, LocalDate.now(), metaInfo));

    }

    private SimpleMailMessage getEmptyMail() {
        SimpleMailMessage mail = new SimpleMailMessage();
        // Prevent SMTPSendFailedException: 554 5.2.0 error:
        // (see https://stackoverflow.com/a/53435444/8457918)
        mail.setFrom(systemAddress);
        return mail;
    }

    private Set<Borrowing> getUnnotifiedBorrowings(String mailType, String recipient, Set<Borrowing> allBorrowings) {

        List<EmailEntry> mailsSentToRecipient = emailRepository.findByRecipientAndType(recipient, mailType);

        Set<Long> borrowingIdsFromMails = mailsSentToRecipient.stream().map(EmailEntry::getMetaInfo)
                .map(map -> map.get(metaInfoBorrowingKey)).flatMap(borrowingIds -> borrowingIds.stream())
                .map(Long::valueOf).collect(toSet());

        Set<Long> borrowingIdsFromCurrentBorrowings = allBorrowings.stream().map(Borrowing::getId).collect(toSet());
        borrowingIdsFromCurrentBorrowings.removeAll(borrowingIdsFromMails);

        // just to make it more readable:
        Set<Long> borrowingIdsOfUnnotifiedBorrowings = borrowingIdsFromCurrentBorrowings;

        return allBorrowings.stream().filter(b -> borrowingIdsOfUnnotifiedBorrowings.contains(b.getId()))
                .collect(toSet());
    }

    @Async
    public void sendOverdueWarningMails(Map<User, Set<Borrowing>> groupedBorrowings) {

        String overdueMailType = "overdueWarningBorrower";

        for (Map.Entry<User, Set<Borrowing>> entry : groupedBorrowings.entrySet()) {

            User user = entry.getKey();
            Set<Borrowing> borrowings = entry.getValue();
            String recipient = user.getEmail();

            Set<Borrowing> unnotifiedOverdueBorrowings = getUnnotifiedBorrowings(overdueMailType, recipient,
                    borrowings);

            if (unnotifiedOverdueBorrowings.size() > 0) {
                EmailTemplate template = templateManager.getTemplate(overdueMailType);
                template.setRecipient(recipient);
                template.setUser(user.getName());
                List<String> borrowedMedia = new ArrayList<>();
                for (Borrowing borrowing : unnotifiedOverdueBorrowings) {
                    borrowedMedia.add("Physical " + borrowing.getPhysical().getId() + ": Due date "
                            + borrowing.getDueDate().toString());
                }
                template.setBorrowedMedia(borrowedMedia);

                try {
                    Map<String, List<String>> metaInfo = new HashMap<>();
                    metaInfo.put(metaInfoBorrowingKey, unnotifiedOverdueBorrowings.stream().map(Borrowing::getId)
                            .map(String::valueOf).collect(toList()));

                    // only send email, if the user activated notifications
                    if (user.getSettings().getGetOverdueInfoAsBorrower()) {
                        sendMail(overdueMailType, template, metaInfo);
                    }

                    // Inform staff members
                    if (user.getRole() == Role.ROLE_STUDENT) {
                        for (Borrowing borrowing : unnotifiedOverdueBorrowings) {
                            sendInfoMailAboutStudentBorrowingToStaffMember(user, borrowing);
                        }
                    } else {
                        for (Borrowing borrowing : unnotifiedOverdueBorrowings) {
                            sendOverdueWarningMailToOwner(borrowing);
                        }
                    }

                } catch (EmailServiceException e) {
                    logger.warn(this.getClass().getName(), e);
                }
            }
        }
    }

    public void sendOverdueWarningMailToOwner(Borrowing borrowing) {
        String mailType = "overdueWarningOwner";
        User owner = borrowing.getMedium().getOwner();
        // if owner deactivated notifications
        if (!owner.getSettings().getGetOverdueInfoAsOwner()) {
            return;
        }

        User borrower = borrowing.getBorrower();
        String recipient = owner.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(borrower.getName());
        template.setStaffMember(owner.getName());
        template.setUserMail(borrower.getEmail());
        String borrowedPhysical = "Physical " + borrowing.getPhysical().getId() + ": "
                + borrowing.getMedium().getMedium().getBooktitle() + "\nBorrow Date: "
                + borrowing.getBorrowDate().toString() + "\nDue Date: " + borrowing.getDueDate().toString();
        template.setPhysical(borrowedPhysical);

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            sendMail(mailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    @Async
    public void sendReturnInfo(Borrowing borrowing) {
        // only send notifications, if the users activated them
        if (borrowing.getBorrower().getSettings().getGetReturnInfoAsBorrower()) {
            this.sendReturnInfoToBorrower(borrowing);
        }
        if (borrowing.getPhysical().getOwner().getSettings().getGetReturnInfoAsOwner()) {
            this.sendReturnInfoToOwner(borrowing);
        }
    }

    public void sendReturnInfoToOwner(Borrowing borrowing) {
        String mailType = "returnInfoOwner";
        User owner = borrowing.getMedium().getOwner();
        User borrower = borrowing.getBorrower();
        String recipient = owner.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(borrower.getName());
        template.setStaffMember(owner.getName());

        String borrowedPhysical = "Physical " + borrowing.getPhysical().getId() + ": "
                + borrowing.getMedium().getMedium().getBooktitle() + "\nBorrow Date: "
                + borrowing.getBorrowDate().toString() + "\nDue Date: " + borrowing.getDueDate().toString()
                + "\nReturn Date: " + borrowing.getReturnDate().toString();
        template.setPhysical(borrowedPhysical);

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            sendMail(mailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    public void sendReturnInfoToBorrower(Borrowing borrowing) {
        String mailType = "returnInfoBorrower";
        User user = borrowing.getBorrower();
        String recipient = user.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(user.getName());

        String borrowedPhysical = "Physical " + borrowing.getPhysical().getId() + ": "
                + borrowing.getMedium().getMedium().getBooktitle() + "\nBorrow Date: "
                + borrowing.getBorrowDate().toString() + "\nDue Date: " + borrowing.getDueDate().toString()
                + "\nReturn Date: " + borrowing.getReturnDate().toString();
        template.setPhysical(borrowedPhysical);

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            sendMail(mailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    @Async
    public void sendExtensionInfo(Borrowing borrowing) {
        // only send notifications, if the users activated them
        if (borrowing.getBorrower().getSettings().getGetExtensionInfoAsBorrower()) {
            this.sendExtensionInfoToBorrower(borrowing);
        }
        if (borrowing.getMedium().getOwner().getSettings().getGetExtensionInfoAsOwner()) {
            this.sendExtensionInfoToOwner(borrowing);
        }
    }

    public void sendExtensionInfoToOwner(Borrowing borrowing) {
        String mailType = "extensionInfoOwner";
        User owner = borrowing.getPhysical().getOwner();
        User borrower = borrowing.getBorrower();
        String recipient = owner.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(borrower.getName());
        template.setStaffMember(owner.getName());
        String borrowedPhysical = "Physical " + borrowing.getPhysical().getId() + ": "
                + borrowing.getMedium().getMedium().getBooktitle() + "\nBorrow Date: "
                + borrowing.getBorrowDate().toString() + "\nNew Due Date: " + borrowing.getDueDate().toString();
        template.setPhysical(borrowedPhysical);

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            sendMail(mailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    public void sendExtensionInfoToBorrower(Borrowing borrowing) {
        String mailType = "extensionInfoBorrower";
        User user = borrowing.getBorrower();
        String recipient = user.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(user.getName());

        String borrowedPhysical = "Physical " + borrowing.getPhysical().getId() + ": "
                + borrowing.getMedium().getMedium().getBooktitle() + "\nBorrow Date: "
                + borrowing.getBorrowDate().toString() + "\nNew Due Date: " + borrowing.getDueDate().toString();
        template.setPhysical(borrowedPhysical);

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            sendMail(mailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    @Async
    public void sendBorrowInfo(Borrowing borrowing) {
        // only send notifications, if the users activated them
        if (borrowing.getPhysical().getOwner().getSettings().getGetBorrowInfoAsOwner()) {
            this.sendBorrowInfoToOwner(borrowing);
        }
        if (borrowing.getBorrower().getSettings().getGetBorrowInfoAsBorrower()) {
            this.sendBorrowInfoToBorrower(borrowing);
        }
    }

    public void sendBorrowInfoToOwner(Borrowing borrowing) {
        String mailType = "borrowInfoOwner";
        User owner = borrowing.getPhysical().getOwner();
        User borrower = borrowing.getBorrower();
        String recipient = owner.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setStaffMember(owner.getName());
        template.setUser(borrower.getName());

        String borrowedPhysical  = String.format("Physical %d : %s \nBorrow Date:  %s \nDue Date: %s", borrowing.getPhysical().getId(),
                borrowing.getMedium().getMedium().getBooktitle(), borrowing.getBorrowDate().toString(),
                borrowing.getDueDate().toString());

        
        template.setPhysical(borrowedPhysical);

        try {
            sendMail(mailType, template, new HashMap<String, List<String>>());
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    public void sendBorrowInfoToBorrower(Borrowing borrowing) {
        String mailType = "borrowInfoBorrower";
        User user = borrowing.getBorrower();
        String recipient = user.getEmail();

        EmailTemplate template = templateManager.getTemplate(mailType);
        template.setRecipient(recipient);
        template.setUser(user.getName());

        String borrowedPhysical  = String.format("Physical %d : %s \nBorrow Date:  %s \nDue Date: %s", borrowing.getPhysical().getId(),
                borrowing.getMedium().getMedium().getBooktitle(), borrowing.getBorrowDate().toString(),
                borrowing.getDueDate().toString());
        template.setPhysical(borrowedPhysical);

        try {
            sendMail(mailType, template, new HashMap<String, List<String>>());
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }
    }

    @Async
    public void sendExpirationWarningMails(Map<User, Set<Borrowing>> groupedBorrowings) {
        String expirationMailType = "expirationWarning";

        for (Map.Entry<User, Set<Borrowing>> entry : groupedBorrowings.entrySet()) {

            User user = entry.getKey();
            // break, if the user deactivated notification
            if (!user.getSettings().getGetExpireInfoAsBorrower()) {
                break;
            }
            Set<Borrowing> borrowings = entry.getValue();
            String recipient = user.getEmail();

            Set<Borrowing> unnotifiedExpirationBorrowings = getUnnotifiedBorrowings(expirationMailType, recipient,
                    borrowings);

            if (!unnotifiedExpirationBorrowings.isEmpty()) {
                EmailTemplate template = templateManager.getTemplate(expirationMailType);
                // setup Mail Template:
                template.setRecipient(recipient);
                template.setUser(user.getName());
                List<String> borrowedMedia = new ArrayList<>();
                for (Borrowing borrowing : unnotifiedExpirationBorrowings) {
                    borrowedMedia.add(
                            borrowing.getPhysical().toString() + " (due at " + borrowing.getDueDate().toString() + ")");
                }
                template.setBorrowedMedia(borrowedMedia);

                try {
                    Map<String, List<String>> metaInfo = new HashMap<>();
                    metaInfo.put(metaInfoBorrowingKey, unnotifiedExpirationBorrowings.stream().map(Borrowing::getId)
                            .map(String::valueOf).collect(toList()));
                    sendMail(expirationMailType, template, metaInfo);
                } catch (EmailServiceException e) {
                    logger.warn(this.getClass().getName(), e);
                }
            }
        }
    }

    private void sendInfoMailToAdmin(User user, Borrowing borrowing) {

        String adminInfoMailType = "overdueAdminInfo";

        EmailTemplate template = templateManager.getTemplate(adminInfoMailType);
        String recipient = adminAddress;

        template.setRecipient(recipient);
        Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
        if (mediumOptional.isPresent()) {
            Medium medium = mediumOptional.get();
            template.setTitle(medium.getShortInfo());
        } else {
            template.setTitle("medium");
        }
        template.setPhysical(borrowing.getPhysical().toString());
        template.setUser(user.getName());
        template.setUserMail(user.getEmail());
        Long overDueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
        template.setOverdueDays(overDueDays.toString());

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            metaInfo.put(metaInfoBorrowingKey, new ArrayList<>(Arrays.asList(borrowing.getId().toString())));
            sendMail(adminInfoMailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }

    }

    private void sendInfoMailAboutStudentBorrowingToStaffMember(User student, Borrowing borrowing) {

        String staffInfoMailType = "overdueStudentStaffInfo";

        EmailTemplate template = templateManager.getTemplate(staffInfoMailType);

        User staffMember = borrowing.getResponsibleStaff();

        template.setRecipient(staffMember.getEmail());
        template.setStaffMember(staffMember.getName());
        Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
        if (mediumOptional.isPresent()) {
            Medium medium = mediumOptional.get();
            template.setTitle(medium.getShortInfo());
        } else {
            template.setTitle("medium");
        }
        template.setPhysical(borrowing.getPhysical().toString());
        template.setUser(student.getName());
        template.setUserMail(student.getEmail());

        Long overDueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());

        if (overDueDays == 1) {
            template.setOverdueDays("yesterday");
        } else {
            template.setOverdueDays(overDueDays.toString() + " days");
        }

        try {
            Map<String, List<String>> metaInfo = new HashMap<>();
            metaInfo.put(metaInfoBorrowingKey, new ArrayList<>(Arrays.asList(borrowing.getId().toString())));
            sendMail(staffInfoMailType, template, metaInfo);
        } catch (EmailServiceException e) {
            logger.warn(this.getClass().getName(), e);
        }

    }

    @Async
    public void sendOverdueInfoMails(Map<User, Set<Borrowing>> groupedBorrowings) {

        for (Map.Entry<User, Set<Borrowing>> entry : groupedBorrowings.entrySet()) {

            User user = entry.getKey();
            Set<Borrowing> borrowings = entry.getValue();
            String recipient = user.getEmail();

            String mailType;
            if (user.getRole() == Role.ROLE_STUDENT) {
                mailType = "overdueStudentInfo";
            } else {
                mailType = "overdueUserInfo";
            }

            Set<Borrowing> unnotifiedCriticalOverdueBorrowings = getUnnotifiedBorrowings(mailType, recipient,
                    borrowings);

            if (unnotifiedCriticalOverdueBorrowings.size() > 0) {

                // We use one template object for both user and student mails
                EmailTemplate template = templateManager.getTemplate(mailType);

                for (Borrowing borrowing : unnotifiedCriticalOverdueBorrowings) {

                    template.setRecipient(recipient);
                    Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
                    if (mediumOptional.isPresent()) {
                        Medium medium = mediumOptional.get();
                        template.setTitle(medium.getShortInfo());
                    } else {
                        template.setTitle("medium");
                    }
                    template.setPhysical(borrowing.getPhysical().toString());
                    template.setUser(user.getName());
                    template.setUserMail(user.getEmail());
                    Long overDueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
                    template.setOverdueDays(overDueDays.toString());
                    if (borrowing.getResponsibleStaff() != null) {
                        template.setStaffMember(borrowing.getResponsibleStaff().getName());
                        template.setStaffMemberMail(borrowing.getResponsibleStaff().getEmail());
                    }

                    try {
                        Map<String, List<String>> metaInfo = new HashMap<>();
                        metaInfo.put(metaInfoBorrowingKey,
                                new ArrayList<>(Arrays.asList(borrowing.getId().toString())));

                        // only send notifications, if the user activated them
                        if (user.getSettings().getGetOverdueInfoAsBorrower()) {
                            sendMail(mailType, template, metaInfo);
                        }

                        // Additionally Inform staff members or admin
                        if (user.getRole() == Role.ROLE_STUDENT) {
                            sendInfoMailAboutStudentBorrowingToStaffMember(user, borrowing);
                        } else {
                            sendInfoMailToAdmin(user, borrowing);
                        }

                    } catch (EmailServiceException e) {
                        logger.warn(this.getClass().getName(), e);
                    }

                }

            }

        }

    }

    // Notifications for Wishlist

    // E-mail Export
    @Async
    public void sendWishesToUniversityLibrary(List<Wish> selectedWishes) throws EmailServiceException {

        String mailType = "uniLibraryOrder";

        EmailTemplate template = templateManager.getTemplate(mailType);

        template.setRecipient(universityLibraryAddress);

        List<String> wishList = new ArrayList<>();

        for (Wish wish : selectedWishes) {
            String url = wish.getUrl();
            if (url != null && !url.isEmpty()) {
                wishList.add(wish.getTitle() + ". (" + url + ")");
            } else {
                wishList.add(wish.getTitle());
            }

        }

        template.setWishList(wishList);

        Map<String, List<String>> metaInfo = new HashMap<>();

        metaInfo.put(metaInfoWishesKey, selectedWishes.stream().map(Wish::getTitle).collect(toList()));

        sendMail(mailType, template, metaInfo);
    }

    // Notification if a new Item is added to the Wishlist
    @Async
    public void sendWishListNewItems(Wish wish) throws EmailServiceException {

        String mailType = "addedNewItem";

        EmailTemplate template = templateManager.getTemplate(mailType);

        template.setRecipient(wishlistEmail);

        List<String> wishList = new ArrayList<>();
        wishList.add(wish.getTitle() + ". (" + wish.getIsbn() + ")");

        template.setWishList(wishList);

        Map<String, List<String>> metaInfo = new HashMap<>();

        sendMail(mailType, template, metaInfo);

    }

    // Notifi. when Wishlist is Updated
    @Async
    public void sendWishListUpdates(List<Wish> selectedWishes) throws EmailServiceException {

        String mailType = "wishlistUpdated";

        EmailTemplate template = templateManager.getTemplate(mailType);

        template.setRecipient(wishlistEmail);

        List<String> wishList = new ArrayList<>();

        for (Wish wish : selectedWishes) {
            String isbn = wish.getIsbn();

            wishList.add(wish.getTitle() + ". (" + isbn + ")");
        }

        template.setWishList(wishList);

        Map<String, List<String>> metaInfo = new HashMap<>();

        metaInfo.put(metaInfoWishesKey, selectedWishes.stream().map(Wish::getTitle).collect(toList()));

        sendMail(mailType, template, metaInfo);
    }

    @Async
    public void sendReadyToBorrowInfo(Reservation next, PhysicalMedium physical) throws EmailServiceException {
        // settings Check
        if (next.getUser().getSettings().isGetReservationInfo()) {

            String mailType = "nextOnWaitingList";
            EmailTemplate template = templateManager.getTemplate(mailType);

            template.setRecipient(next.getUser().getEmail());
            template.setUser(next.getUser().getName());
            template.setTitle(physical.getMedium().getBooktitle());

            sendMail(mailType, template, new HashMap<String, List<String>>());
        }
    }

    @Async
    public void sendMissedToBorrow(Reservation next, PhysicalMedium physical) throws EmailServiceException {

        if (next.getUser().getSettings().getGetMissedReservationInfo()) {
            String mailType = "missedToBorrow";

            EmailTemplate template = templateManager.getTemplate(mailType);

            template.setRecipient(next.getUser().getEmail());
            template.setUser(next.getUser().getName());
            template.setTitle(physical.getMedium().getBooktitle());

            sendMail(mailType, template, new HashMap<String, List<String>>());
        }
    }
}