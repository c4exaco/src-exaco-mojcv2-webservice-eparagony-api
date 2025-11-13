import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.helpers.ReceiptApiHelper
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.dto.ReceiptSolrModel
import pl.exaco.receiptApi.model.transaction.DiscountType
import pl.exaco.receiptApi.model.transaction.MobileItem
import pl.exaco.receiptApi.model.transaction.MobilePayment
import pl.exaco.receiptApi.model.transaction.TransactionForMobile
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Unroll
class ReceiptApiHelperGroovyTest extends Specification {

    ReceiptApiHelper helper

    def setup() {
        given:
        def appConfig = Mock(AppConfig)
        helper = new ReceiptApiHelper(appConfig)
    }

    def "translateDiscountType enum #disType should result with #result" (DiscountType disType) {

        expect:
        helper.translateDiscountType(disType) == result

        where:
        disType                                 || result
        DiscountType.MANUAL_ITEM                || disType.translationPL
        DiscountType.MANUAL_TRANSACTION         || disType.translationPL
        DiscountType.PROMOTION_ITEM             || disType.translationPL
        DiscountType.PROMOTION_TRANSACTION      || disType.translationPL
        DiscountType.PRICE_RULE                 || disType.translationPL
        DiscountType.PRICE_LEVEL                || disType.translationPL
        DiscountType.LOYALTY_TRANSACTION        || disType.translationPL
        DiscountType.POS_LOGIC_TRANSACTION      || disType.translationPL
        DiscountType.PAYMENT_FIXED_TRANSACTION  || disType.translationPL
        DiscountType.CUSTOMER_FIXED_TRANSACTION || disType.translationPL
        DiscountType.MANUAL_FIXED_TRANSACTION   || disType.translationPL
        DiscountType.PERCENT_LAST_ITEM          || disType.translationPL
        DiscountType.COUPON_ITEM                || disType.translationPL
    }

    def "getTotalDiscount for #list should return correct #result" (List<MobileItem> list) {

        expect:
        helper.getTotalDiscount(list) == result

        where:
        list                                                                                  || result
        Arrays.asList(new MobileItem("10.2"), new MobileItem("5.2"), new MobileItem("10.3"))  || BigDecimal.valueOf(25.7)
        Arrays.asList(new MobileItem("0.00"), new MobileItem("0.00"), new MobileItem("0.00")) || null
        Arrays.asList(new MobileItem(null), new MobileItem(null), new MobileItem(null))       || null
    }

    def "getPreparedFiledQuery for #cards should return correct #result" (Set<CustomerCard> cards) {

        expect:
        helper.getPreparedFiledQuery(cards) == result

        where:
        cards                                                                                               || result
        Arrays.asList(new CustomerCard("0012")).toSet()                                                     || "loyaltyCardNumber:0012"
        Arrays.asList(new CustomerCard("0012"), new CustomerCard("0013"), new CustomerCard("0014"))         || "loyaltyCardNumber:(0012 OR 0013 OR 0014)"
    }

    def "preparePaymentSet for #mongoData should return correct #result" (TransactionForMobile mongoData) {

        expect:
        helper.preparePaymentSet(mongoData)?.find { it.type == 0 }?.getAmount() == result

        where:
        mongoData                                                                                                                                         || result
        new TransactionForMobile("245.50", Arrays.asList(new MobilePayment(5, "100.00", null, null), new MobilePayment(0, "150.00", null, null)), "5.50") || BigDecimal.valueOf(144.50).setScale(2)
        new TransactionForMobile("245.50", Arrays.asList(new MobilePayment(5, "100.00", null, null), new MobilePayment(0, "150.00", null, null)), "0")    || BigDecimal.valueOf(150.00).setScale(2)
        new TransactionForMobile("245.50", Arrays.asList(new MobilePayment(5, "100.00", null, null), new MobilePayment(0, "150.00", null, null)), null)   || BigDecimal.valueOf(150.00).setScale(2)
    }

    def "getSearchPhrase for #query should return correct #result" (String query) {

        expect:
        helper.getSearchPhrase$receiptApi(query) == result

        where:
        query                                                       || result
        "Kiszka%20Pi%C4%85tnica"                                    || "*kiszka*piątnica*"
        "Kiszka%20Pi%C4%85tnica%20Smaczna"                          || "*kiszka*piątnica*smaczna*"
        "Kiszka%20%20%20Pi%C4%85tnica%20%20%20%20%20%20%20Smaczna"  || "*kiszka*piątnica*smaczna*"
    }

    def "getArticleNameForConverter for #mongoName, #solrName, #item should return correct #result" (String mongoName, String solrName, MobileItem item) {

        expect:
        helper.getArticleNameForConverter(mongoName, solrName, item) == result

        where:
        mongoName   | solrName      | item                                 || result
        "!*produkt" | null          | null                                 || "Produkt"
        null        | "!*produkt"   | null                                 || "Produkt"
        null        | null          | null                                 || "Brak nazwy artykułu"

    }

    def "getTotalDateTime for solrModel #model one of date not null"(ReceiptSolrModel model) {

        expect:
        helper.getTotalDateTime(model) == result

        where:

        model                                                                                || result
        new ReceiptSolrModel(
                Date.from(
                        Instant.ofEpochMilli(1649830558000)
                ), null)                                                                     || ZonedDateTime.ofInstant(Instant.ofEpochMilli(1649830558000), ZoneId.systemDefault())
        new ReceiptSolrModel(new Date(), [1449830558000])                                    || ZonedDateTime.ofInstant(Instant.ofEpochMilli(1449830558000), ZoneId.systemDefault())

    }

    def "getTotalDateTime for solrModel #model both dates null, return zonedDateTimeNow"(ReceiptSolrModel model) {

        expect:
        helper.getTotalDateTime(model) > result

        where:

        model                                                                           || result
        new ReceiptSolrModel()                                                          || ZonedDateTime.now()

    }

    def "getSolrArticleBarcode for eforce request test #result" (String solrItem) {

        expect:
        ReceiptApiHelper.@Companion.getSolrArticleBarcode(solrItem) == result

        where:
        solrItem                     || result
        "Cola;6.60;10"               || "10"
        "Cola;6.60;0000123456"       || "0000123456"
        "Cola;6.60;0000012345622"    || "12345622"
        "Cola;6.60;1234000001234"    || "1234000001234"
        "Cola;6.60;2234123400000"    || "2234123400000"
        "Cola;6.60;0000000000000"    || "00000000"
        "Cola;6.60;"                 || ""
        ";6.60;1"                    || "1"

    }

    def "getSolrArticlePrice for eforce request test #result" (String solrItem) {

        expect:
        ReceiptApiHelper.@Companion.getSolrArticlePrice(solrItem) == result

        where:
        solrItem                        || result
        "C;o;la;;6.60;10"               || "6.60"
        "Co;;;;la;6.60;0000123456"      || "6.60"
        "Cola;6.60;0000012345622"       || "6.60"
        "C;ola;6.60;1234000001234"      || "6.60"
        "Cola;6.60;2234123400000"       || "6.60"
        "C;ol;a;6.60;0000000000000"     || "6.60"
        "Co;la;6.60;"                   || "6.60"
        "C;ola;6.60;1"                  || "6.60"

    }


    def "getSolrArticleName for eforce request test #result" (String solrItem) {

        expect:
        ReceiptApiHelper.@Companion.getSolrArticleName(solrItem) == result

        where:
        solrItem                        || result
        "C;o;la;;6.60;10"               || "C;o;la;"
        "Co;;;;la;6.60;0000123456"      || "Co;;;;la"
        ";6.60;0000012345622"           || ""
        "C;ola;6.60;1234000001234"      || "C;ola"
        "Cola;6.60;2234123400000"       || "Cola"
        "C;ol;a;6.60;0000000000000"     || "C;ol;a"
        "Co;la;6.60;"                   || "Co;la"
        "C;ola;6.60;1"                  || "C;ola"

    }
}