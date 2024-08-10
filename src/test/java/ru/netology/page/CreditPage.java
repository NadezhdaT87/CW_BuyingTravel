package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;


public class CreditPage {


        private SelenideElement buyCreditCardButton = $("div > button:nth-child(4) > span > span");
        private SelenideElement headingCreditPay = $(byText("Кредит по данным карты"));
        private SelenideElement cardNumder = $("form fieldset .input input");
        private SelenideElement month = $("form fieldset .input-group .input input");
        private SelenideElement year = $("form fieldset .input-group > span:nth-child(2) .input input");
        private SelenideElement cardHolder = $("form fieldset > div:nth-child(3) .input-group [view='default']");
        private SelenideElement cardCode = $("form > fieldset > div:nth-child(3) > .input-group > span:nth-child(2) .input input");
        private SelenideElement buttonSubmit = $("form fieldset button");
        private SelenideElement notificationChecking = $(".notification");
        private SelenideElement notificationOkContent = $(".notification_status_ok .notification__content");
        private SelenideElement notificationStatusOk = $(".notification_status_ok");
        private SelenideElement notificationErrorContent = $(".notification_status_error .notification__content");
        private SelenideElement inputInvalid = $(".input_invalid");

        public void openCreditPayPage() {
            buyCreditCardButton.click();
            headingCreditPay.shouldBe(visible);
        }

        public void waitingNotification() {
            notificationChecking.shouldHave(visible, Duration.ofSeconds(15));
        }

        public void shouldNotificationSuccessfulText(String expectedText) {
            notificationOkContent.shouldHave(exactText(expectedText)).shouldBe(visible, Duration.ofSeconds(15));
        }

        public void shouldNotificationUnsuccessfulText(String expectedText) {
            notificationErrorContent.shouldHave(exactText(expectedText)).shouldBe(visible, Duration.ofSeconds(15));
        }

        public void shouldOkNotificationInvisibile() {
            notificationStatusOk.shouldNotBe(visible).should(disappear, Duration.ofSeconds(15));
        }

        public void checkInputInvalid(String expectedText) {
            inputInvalid.shouldHave(exactText(expectedText)).shouldBe(visible, Duration.ofSeconds(5));
        }

        public void creditByCard(DataHelper.CardInfo cardInfo) {
            cardNumder.setValue(cardInfo.getNumber());
            month.setValue(cardInfo.getMonth());
            year.setValue(cardInfo.getYear());
            cardHolder.setValue(cardInfo.getHolder());
            cardCode.setValue(cardInfo.getCvcCode());
            buttonSubmit.click();
        }
    }

