package com.example.core.localization

enum class AppLanguage(val code: String, val title: String) {
  ENGLISH("en", "English"),
  BANGLA("bn", "বাংলা")
}

object AppStrings {
  fun appTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "স্মার্ট মেস ম্যানেজার"
    AppLanguage.ENGLISH -> "Smart Mess Manager"
  }

  fun home(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "হোম"
    AppLanguage.ENGLISH -> "Home"
  }

  fun members(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সদস্য"
    AppLanguage.ENGLISH -> "Members"
  }

  fun meals(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মিল শীট"
    AppLanguage.ENGLISH -> "Meals"
  }

  fun bazar(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বাজার"
    AppLanguage.ENGLISH -> "Bazar"
  }

  fun deposits(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "জমা"
    AppLanguage.ENGLISH -> "Deposits"
  }

  fun bills(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বিল ও খরচ"
    AppLanguage.ENGLISH -> "Bills"
  }

  fun summary(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "হিসাব সারসংক্ষেপ"
    AppLanguage.ENGLISH -> "Summary"
  }

  fun charts(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গ্রাফ ও রিপোর্ট"
    AppLanguage.ENGLISH -> "Reports"
  }

  fun menu(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেনু ও রুটিন"
    AppLanguage.ENGLISH -> "Menu & Duty"
  }

  fun notices(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নোটিশ বোর্ড"
    AppLanguage.ENGLISH -> "Notices"
  }

  fun settings(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সেটিংস"
    AppLanguage.ENGLISH -> "Settings"
  }

  fun totalMeals(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট মিল"
    AppLanguage.ENGLISH -> "Total Meals"
  }

  fun totalDeposit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট জমা"
    AppLanguage.ENGLISH -> "Total Deposit"
  }

  fun bazarCost(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বাজার খরচ"
    AppLanguage.ENGLISH -> "Bazar Cost"
  }

  fun totalBills(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট ইউটিলিটি বিল"
    AppLanguage.ENGLISH -> "Total Bills"
  }

  fun messBalance(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ম্যানেজারের হাতে জমা (ক্যাশ)"
    AppLanguage.ENGLISH -> "Mess Cash in Hand"
  }

  fun mealRate(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাইভ মিল রেট"
    AppLanguage.ENGLISH -> "Live Meal Rate"
  }

  fun mySummary(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আমার সারসংক্ষেপ"
    AppLanguage.ENGLISH -> "My Summary"
  }

  fun myMeals(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আমার মিল"
    AppLanguage.ENGLISH -> "My Meals"
  }

  fun myCost(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আমার মোট খরচ"
    AppLanguage.ENGLISH -> "My Total Cost"
  }

  fun myDeposit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আমার জমা"
    AppLanguage.ENGLISH -> "My Deposit"
  }

  fun myBalance(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আমার বর্তমান ব্যালেন্স"
    AppLanguage.ENGLISH -> "My Net Balance"
  }

  fun messOwesYou(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেসের কাছে পাওনা (ফেরতযোগ্য)"
    AppLanguage.ENGLISH -> "Mess owes you (Credit)"
  }

  fun youOweMess(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেসের বকেয়া দেনা"
    AppLanguage.ENGLISH -> "You owe the mess (Due)"
  }

  fun todayOverview(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আজকের মিল সংখ্যা"
    AppLanguage.ENGLISH -> "Today's Meal Count"
  }

  fun breakfast(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সকালের নাস্তা"
    AppLanguage.ENGLISH -> "Breakfast"
  }

  fun lunch(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "দুপুরের খাবার"
    AppLanguage.ENGLISH -> "Lunch"
  }

  fun dinner(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "রাতের খাবার"
    AppLanguage.ENGLISH -> "Dinner"
  }

  fun addMeal(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মিল যোগ করুন"
    AppLanguage.ENGLISH -> "Add Meal"
  }

  fun addBazar(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বাজার হিসাব দিন"
    AppLanguage.ENGLISH -> "Add Bazar"
  }

  fun addDeposit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "টাকা জমা দিন"
    AppLanguage.ENGLISH -> "Add Deposit"
  }

  fun addBill(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বিল যোগ করুন"
    AppLanguage.ENGLISH -> "Add Bill"
  }

  fun addExtraCost(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অতিরিক্ত খরচ / দাওয়াত"
    AppLanguage.ENGLISH -> "Add Extra Cost"
  }

  fun addToDepositCheckbox(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ক্রেতার জমা ব্যালেন্সে এই টাকা যুক্ত করুন"
    AppLanguage.ENGLISH -> "Add this amount to shopper's deposit balance"
  }

  fun auditTrail(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অ্যাক্টিভিটি লগ / অডিট হিস্টোরি"
    AppLanguage.ENGLISH -> "Activity & Audit Log"
  }

  fun closeMonth(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মাস ক্লোজ / সংরক্ষণ করুন"
    AppLanguage.ENGLISH -> "Close & Archive Month"
  }

  fun reopenMonth(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মাস পুনরায় চালু করুন"
    AppLanguage.ENGLISH -> "Reopen Month"
  }

  fun demoRoleSwitch(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ইউজার রোল পরিবর্তন (টেস্টিং)"
    AppLanguage.ENGLISH -> "Switch Active Role (Demo)"
  }

  fun exportPdf(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পিডিএফ রিপোর্ট ডাউনলোড"
    AppLanguage.ENGLISH -> "Export PDF Report"
  }

  fun exportCsv(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এক্সেল / সিএসভি এক্সপোর্ট"
    AppLanguage.ENGLISH -> "Export Excel / CSV"
  }

  fun sharedCostExplainer(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "শেয়ার্ড কস্ট: মোট বাজার খরচকে মোট মিল সংখ্যা দিয়ে ভাগ করে সঠিক মিল রেট নির্ধারণ করা হয়।"
    AppLanguage.ENGLISH -> "Shared Cost: Total bazar expenditure divided by total consumed meals gives the live meal rate."
  }

  fun fixedRateExplainer(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফিক্সড রেট: ম্যানেজার নির্ধারিত নির্দিষ্ট রেটে সবার মিল খরচ গণনা করা হয়।"
    AppLanguage.ENGLISH -> "Fixed Rate: Meals are billed at a fixed predefined rate set by the mess manager."
  }
}
