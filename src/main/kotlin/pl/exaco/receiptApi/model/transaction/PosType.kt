package pl.exaco.receiptApi.model.transaction

enum class PosType(val value: String) {

    LINE("LINE"),
    SCO("SCO"),
    PETROL_STATION("PETROL_STATION"),
    VENDING("VENDING"),
    HECTRONIC("HECTRONIC"),
    MOBILE_POS("MOBILE_POS");

}

