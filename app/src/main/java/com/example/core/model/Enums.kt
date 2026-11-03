package com.example.core.model

enum class Role(val label: String) {
  MANAGER("Manager"),
  SUB_MANAGER("Sub-Manager"),
  MEMBER("Member")
}

enum class MealSystem(val title: String) {
  SHARED_COST("Shared Cost"),
  FIXED_RATE("Fixed Rate")
}

enum class PaymentMethod(val label: String) {
  CASH("Cash"),
  BKASH("bKash"),
  NAGAD("Nagad"),
  BANK("Bank")
}

enum class BillType(val displayName: String) {
  GAS("Gas"),
  ELECTRICITY("Electricity / Current"),
  MAID("Maid / Bua"),
  INTERNET("Internet / WiFi"),
  RENT("House Rent"),
  OTHER("Other Utility")
}

enum class SplitMode(val label: String) {
  SHARED_EQUALLY("Shared Equally (All)"),
  PER_PERSON("Fixed Per Person"),
  SELECTED_MEMBERS("Selected Members Only")
}

enum class AuditAction {
  CREATE,
  UPDATE,
  DELETE,
  CLOSE_MONTH
}
