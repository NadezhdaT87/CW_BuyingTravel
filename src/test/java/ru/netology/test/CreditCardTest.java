package ru.netology.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;
import ru.netology.page.CreditPage;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.logevents.SelenideLogger.step;
import static org.junit.jupiter.api.Assertions.*;

public class CreditCardTest {
    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:8080");
        var creditPage = new CreditPage();
        creditPage.openCreditPayPage();
    }

    @DisplayName("Успешная покупка в кредит с валидными данными карты со статусом APPROVED")
    @Test
    public void successfulPurchaseWithValidCard() {
        var cardNumber = DataHelper.approvedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder= DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
            creditPage.waitingNotification();
        });

        var expectedStatus = "APPROVED";
        var actualStatus = SQLHelper.getInfoFromCreditPayment().getStatus();
        var bankID = SQLHelper.getInfoFromCreditPayment().getBank_id();
        var paymentID = SQLHelper.getInfoFromOrder().getPayment_id();

        assertAll(
                () ->
                        step("Проверка уведомления об оплате", () -> {
                            creditPage.shouldNotificationSuccessfulText("Операция одобрена Банком.");
                        }),
                () ->
                        step("Проверка статуса платежа в БД", () -> {
                            assertEquals(expectedStatus, actualStatus);
                        }),
                () ->
                        step("Проверка платежа в таблице заказов в БД", () -> {
                            assertEquals(bankID, paymentID);
                        }));
    }

    @DisplayName("Отклонение оплаты в кредит с карты со статусом DECLINED")
    @Test
    public void paymentRejectionWithCardDeclined() {
        var cardNumber = DataHelper.declinedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
            creditPage.waitingNotification();
        });

        var expectedStatus = "DECLINED";
        var actualStatus = SQLHelper.getInfoFromCreditPayment().getStatus();
        var bankID = SQLHelper.getInfoFromCreditPayment().getBank_id();
        var paymentID = SQLHelper.getInfoFromOrder().getPayment_id();

        assertAll(
                () ->
                        step("Проверка уведомления об ошибке", () -> {
                            creditPage.shouldNotificationSuccessfulText("Ошибка! Банк отказал в проведении операции.");
                        }),
                () ->
                        step("Проверка статуса платежа в БД", () -> {
                            assertEquals(expectedStatus, actualStatus);
                        }),
                () ->
                        step("Проверка отсутствия платежа со статусом Declined в таблице заказов", () -> {
                            assertNotEquals(bankID, paymentID);
                        })
        );
    }
    @Test
    @DisplayName("Отклонение оплаты в кредит с недействительным номером карты")
    void paymentRejectionWithInvalidCard() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
            creditPage.waitingNotification();
        });

        assertAll(
                () ->
                        step("Проверка уведомления об ошибке", () -> {
                            creditPage.shouldNotificationUnsuccessfulText("Ошибка! Банк отказал в проведении операции.");
                        }),
                () ->
                        step("Проверка отсутствия видимости уведомления об успехе", creditPage::shouldOkNotificationInvisibile)
        );
    }

    @DisplayName("Неуспешная оплата в кредит без указания номера карты")
    @Test
    public void unsuccessfulPaymentWithoutCard() {
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo("", month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Номер карты Неверный формат");
        });
    }
    @DisplayName("Неуспешная попытка оплаты в кредит с 15-значным номером карты")
    @Test
    public void unsuccessfulPaymentWith15DigitCardNumber() {
        var cardNumber = DataHelper.getInvalidCardNumberLessThan16();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Номер карты Неверный формат");
        });
    }

    @DisplayName("Неуспешная оплата в кредит без указания месяца карты")
    @Test
    public void unsuccessfulPaymentWithoutMonth() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, "", year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Месяц Неверный формат");
        });
    }
    @DisplayName("Неуспешная оплата в кредит с указанием прошлого месяца текущего года карты")
    @Test
    public void unsuccessfulPaymentWithCurrentMonthAndYear() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getCurrentMonthAndYear().getCardMonth();
        var year = DataHelper.getCurrentMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Месяц Неверно указан срок действия карты");
        });
    }
    @DisplayName("Неуспешная оплата в кредит с указанием истекшего года карты")
    @Test
    public void unsuccessfulPaymentWithExpiredYear() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getInvalidCardWithPreviousYears().getCardMonth();
        var year = DataHelper.getInvalidCardWithPreviousYears().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Год Истёк срок действия карты");
        });
    }
    @DisplayName("Неуспешная оплата в кредит с указанием будущего года карты(больше 5 лет)")
    @Test
    public void unsuccessfulPaymentOnCreditIndicatingFutureYearCardForMoreThan5() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getInvalidCardWithFutureYears().getCardMonth();
        var year = DataHelper.getInvalidCardWithFutureYears().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Год Неверно указан срок действия карты");
        });
    }

    @DisplayName("Неуспешная оплата в кредит без указания года карты")
    @Test
    public void unsuccessfulPaymentWithoutYear() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, "", cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Год Неверный формат");
        });
    }

    @DisplayName("Неуспешная оплата в кредит с указанием 2-значного номера CVV/CVC карты")
    @Test
    public void failedPaymentWithCodeOfFewerCharacters() {
        var cardNumber = DataHelper.approvedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardCode = DataHelper.getInvalidCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });
        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("CVC/CVV Неверный формат");
        });
    }
    @DisplayName("Неуспешная оплата в кредит без указания номера CVV/CVC карты")
    @Test
    public void unsuccessfulPaymentWithoutCVC() {
        var cardNumber = DataHelper.approvedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getValidCardHolderName();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, "");
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("CVC/CVV Неверный формат");
        });
    }

    @DisplayName("Неуспешная оплата без указания владельца карты")
    @Test
    public void unsuccessfulPaymentWithoutHolder() {
        var cardNumber = DataHelper.getRandomCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, "", cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);
        });
        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Владелец Поле обязательно для заполнения");
        });
    }

    @DisplayName("Неуспешная оплата со спец.символами в поле владелец по карте в кредит")
    @Test
    public void unsuccessfulPaymentWithInvalidHolderSymbols() {
        var cardNumber = DataHelper.approvedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getInvalidCardHolderNameSpecSimbol();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);

        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Владелец Поле обязательно для заполнения");
        });
    }
    @DisplayName("Неуспешная оплата с цифрами в поле владелец по карте в кредит")
    @Test
    public void unsuccessfulPaymentWithInvalidHolderNumbers() {
        var cardNumber = DataHelper.approvedCardNumber();
        var month = DataHelper.getValidMonthAndYear().getCardMonth();
        var year = DataHelper.getValidMonthAndYear().getCardYear();
        var cardHolder = DataHelper.getInvalidCardHolderNameNumber();
        var cardCode = DataHelper.getRandomCardCode();
        var cardInfo = new DataHelper.CardInfo(cardNumber, month, year, cardHolder, cardCode);
        var creditPage = new CreditPage();

        step("Производим оплату", () -> {
            creditPage.creditByCard(cardInfo);

        });

        Allure.step("Проверка уведомления об ошибке", () -> {
            creditPage.checkInputInvalid("Владелец Поле обязательно для заполнения");
        });
    }

}
