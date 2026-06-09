package com.example.ui

enum class AppLanguage {
    EN, BN
}

object Localization {
    
    fun get(key: String, lang: AppLanguage): String {
        val mapping = when (lang) {
            AppLanguage.EN -> englishMap
            AppLanguage.BN -> bengaliMap
        }
        return mapping[key] ?: key
    }

    private val englishMap = mapOf(
        "app_title" to "DyeChem Smart Inventory",
        "nav_dashboard" to "Dashboard",
        "nav_inventory" to "Inventory",
        "nav_consumption" to "Recipe Issue",
        "nav_purchases" to "Purchases",
        "nav_suppliers" to "Suppliers",
        "nav_scanner" to "OCR & QR Scanner",
        "nav_voice" to "Voice Assistant",
        "nav_analytics" to "Analytics",
        "lang_switch" to "বাংলা",
        "lang_name" to "English",
        
        // Dashboard cards
        "total_inventory" to "Total Inventory",
        "total_dye" to "Total Dye",
        "total_chemical" to "Total Chemicals",
        "low_stock" to "Low Stock Items",
        "today_usage" to "Today's Consumption",
        "monthly_usage" to "Monthly Consumption",
        "inventory_value" to "Total Stock Value",
        "quick_action" to "Quick Action",
        "add_stock" to "Add Stock",
        "dispatch" to "Record Consumption",
        "reorder" to "Reorder Now",
        "view_details" to "View Details",
        
        // Scanner Panel
        "camera_title" to "Smart Camera Scan Scanner",
        "qr_camera" to "QR / Label Reader",
        "ocr_info" to "Aim at physical packet label or lot print. Simulated high-end edge OCR extracts batch/shelf immediately.",
        "sample_scanner_header" to "Simulated Camera Capture Target Cards",
        "action_scan_label" to "Scan Product Label",
        "ocr_success" to "OCR Scan successful!",
        "item_loaded" to "Product profile loaded automatically",
        "scan_instruction_1" to "Place dye lottery tag within reticle bounding Box",
        
        // Voice Panel
        "voice_title" to "AI Store Voice Companion",
        "voice_sub" to "Offline local matching + Online smart response engine",
        "voice_instruction_title" to "Try Saying Commands Like:",
        "voice_cmd1" to "\"Red Dye কত আছে?\"",
        "voice_cmd2" to "\"Lot 100 কোথায় আছে?\"",
        "voice_cmd3" to "\"Low Stock দেখাও\"",
        "voice_cmd4" to "\"আজ কত Chemical ব্যবহার হয়েছে?\"",
        "voice_cmd5" to "\"সব Dye দেখাও\"",
        "say_something" to "Tap mic button and speak",
        "micro_denied" to "Audio record state configured. Tap examples above to simulate real voice input instantly!",
        "voice_thinking" to "Consulting smart voice interpreter...",
        
        // Add/Edit Product
        "field_name" to "Product Name",
        "field_code" to "Product Code",
        "field_category" to "Category",
        "field_brand" to "Brand/Manufacturer",
        "field_lot" to "Lot Number",
        "field_batch" to "Batch Number",
        "field_rack" to "Rack Number / Shelf",
        "field_location" to "Warehouse Location / Section",
        "field_unit" to "Measurement Unit",
        "field_open_stock" to "Opening Stock Level",
        "field_low_threshold" to "Low Stock Margin Limit",
        "field_price" to "Purchase Price Unit",
        "field_currency" to "Currency Mode",
        "field_icon" to "Visual Visual Icon",
        "save_product" to "Save Product Details",
        "add_product_title" to "Add New Product Registry",
        
        // Details / Inventory
        "current_stock" to "In-Stock Quantity",
        "warehouse_location" to "Shelf Location",
        "price_value" to "Price Rating",
        "status" to "Level Status",
        "stock_status_good" to "Healthy Stock",
        "stock_status_low" to "Critical Low Stock ALERT!",
        "stock_out" to "Store Dispatch Balance",
        "stock_in" to "Store Receiving Balance",
        "reserved_stock" to "Reserved Allotment",
        "damaged_stock" to "Damaged Store Stock",
        "value" to "Calculated Stock Value",
        "search_placeholder" to "Search product name, code, lot, shelf location...",
        "no_products" to "No matching stock list found in SQLite registry.",
        
        // Consumption Registry
        "log_consumption" to "Log Daily Store Consumption",
        "qty_used" to "Quantity Subtracted",
        "dept" to "Recipient Department",
        "operator" to "Store Dispatch Officer",
        "notes" to "Comments / Transaction Notes",
        "consumption_log" to "Daily Store Dispatch Logbook",
        "save_consumption" to "Authorize Material Dispatch",
        
        // Purchase Panel
        "log_purchase" to "Receive Fresh Raw Materials",
        "supplier" to "Wholesale Supplier",
        "invoice" to "Bill / Invoice reference",
        "purchase_log" to "Historic Material Receipt Logbook",
        
        // Supplier Panel
        "add_supplier" to "Register Standard Supplier",
        "supplier_mobile" to "Primary Contact Number",
        "supplier_addr" to "Supplier Headquarters Location",
        
        // Export & Reports
        "reports_header" to "Smart Factory Reports Center",
        "export_hint" to "Generate and export inventory data locally in multiple formats:",
        "export_pdf" to "Export PDF Report",
        "export_excel" to "Export Excel Sheet",
        "export_csv" to "Export CSV Sheet",
        "backup_restore" to "Local SQLite Database Backups",
        "backup_db" to "Create Auto Backup",
        "restore_db" to "Load Auto Backup",
        "export_success" to "Data log compiled successfully!",
        
        // Security Mode
        "roles_title" to "Operator Security Configuration",
        "current_role" to "Current Security Role",
        "btn_admin" to "Admin (Full Access)",
        "btn_manager" to "Manager (Warehouse + Reports)",
        "btn_keeper" to "StoreKeeper (Deductions + Receipts)",
        "btn_viewer" to "Viewer (Read-Only Safety)"
    )

    private val bengaliMap = mapOf(
        "app_title" to "ডাই-কেম স্মার্ট ইনভেন্টরি",
        "nav_dashboard" to "ড্যাশবোর্ড",
        "nav_inventory" to "পণ্যের তালিকা",
        "nav_consumption" to "রেসিপি ইস্যু",
        "nav_purchases" to "ক্রয় মডিউল",
        "nav_suppliers" to "সরবরাহকারী",
        "nav_scanner" to "ওসিআর ও কিউআর স্ক্যানার",
        "nav_voice" to "ভয়েস অ্যাসিস্ট্যান্ট",
        "nav_analytics" to "অ্যানালিটিক্স",
        "lang_switch" to "English",
        "lang_name" to "বাংলা",
        
        // Dashboard cards
        "total_inventory" to "মোট মজুদ পণ্য",
        "total_dye" to "মোট রঙের মজুদ",
        "total_chemical" to "মোট কেমিক্যালের মজুদ",
        "low_stock" to "কম মজুদসম্পন্ন পণ্য",
        "today_usage" to "আজকের ব্যবহার",
        "monthly_usage" to "চলতি মাসের ব্যবহার",
        "inventory_value" to "মোট মজুদ পণ্যের মূল্য",
        "quick_action" to "দ্রুত অ্যাকশন",
        "add_stock" to "মজুদ আনুন",
        "dispatch" to "ব্যবহার নিবন্ধন",
        "reorder" to "রিঅর্ডার এলার্ট",
        "view_details" to "বিস্তারিত দেখুন",
        
        // Scanner Panel
        "camera_title" to "স্মার্ট ক্যামেরা ওসিআর স্ক্যানার",
        "qr_camera" to "কিউআর এবং লেবেল রিডার",
        "ocr_info" to "প্যাকেটের লেবেল বা লট প্রিন্টের সামনাসামনি লক্ষ্য করুন। আমাদের ওসিআর ইঞ্জিন সরাসরি পণ্যকে চিহ্নিত করে ডাটাবেজ থেকে নিয়ে আসবে।",
        "sample_scanner_header" to "পরীক্ষামূলক ডেমো লেবেল স্ক্যান কার্ড",
        "action_scan_label" to "লেবেল স্ক্যান করুন",
        "ocr_success" to "ওসিআর স্ক্যান সফল হয়েছে!",
        "item_loaded" to "পণ্য সংক্রান্ত তথ্য লোড হয়েছে",
        "scan_instruction_1" to "রঙের লেবেলটি স্ক্যানিং বক্সের মাঝে রাখুন",
        
        // Voice Panel
        "voice_title" to "এআই স্টোর ভয়েস সহকারী",
        "voice_sub" to "অফলাইন লোকাল কমান্ড এবং অনলাইন এআই সমৃদ্ধ ভয়েস সিস্টেম",
        "voice_instruction_title" to "নিচের প্রশ্নগুলো উচ্চারণ করে দেখুন:",
        "voice_cmd1" to "\"Red Dye কত আছে?\"",
        "voice_cmd2" to "\"Lot 100 কোথায় আছে?\"",
        "voice_cmd3" to "\"Low Stock দেখাও\"",
        "voice_cmd4" to "\"আজ কত Chemical ব্যবহার হয়েছে?\"",
        "voice_cmd5" to "\"সব Dye দেখাও\"",
        "say_something" to "কথা বলতে মাইক্রোফোন বাটন স্পর্শ করুন",
        "micro_denied" to "ভয়েস রেকর্ডি অনুমোদিত। সরাসরি ডেমো প্রশ্নগুলো চাপ দিয়ে টেস্ট করতে পারেন!",
        "voice_thinking" to "ভয়েস সহকারী ফলাফল বিশ্লেষণ করছে...",
        
        // Add/Edit Product
        "field_name" to "পণ্যের নাম (বাংলা/ইংরেজি)",
        "field_code" to "প্রোডাক্ট কোড",
        "field_category" to "ক্যাটাগরি",
        "field_brand" to "ব্র্যান্ড / প্রস্তুতকারক",
        "field_lot" to "লট নাম্বার (Lot No)",
        "field_batch" to "ব্যাচ নাম্বার (Batch No)",
        "field_rack" to "র‍্যাক নাম্বার (আলমারি বা সেলফ)",
        "field_location" to "গুদামের অবস্থান (সেকশন)",
        "field_unit" to "পরিমাপক একক",
        "field_open_stock" to "প্রারম্ভিক স্টক মজুদ",
        "field_low_threshold" to "সতর্কতামূলক সর্বনিম্ন স্টক সীমা",
        "field_price" to "ক্রয় মূল্য দর",
        "field_currency" to "কারেন্সি টাইপ (টাকা বা ডলার)",
        "field_icon" to "পছন্দের পণ্য ছবি আইকন",
        "save_product" to "নতুন পণ্য হিসেবে রেজিস্টার করুন",
        "add_product_title" to "নতুন ডাই/কেমিকেল ডাটাবেজে অন্তর্ভুক্ত করুন",
        
        // Details / Inventory
        "current_stock" to "বর্তমান মজুদ পরিমাণ",
        "warehouse_location" to "সংরক্ষণ সেলফ র‍্যাক",
        "price_value" to "ক্রয় মূল্য রেট",
        "status" to "মজুদের সার্বিক অবস্থা",
        "stock_status_good" to "পর্যাপ্ত স্টক মজুদ",
        "stock_status_low" to "নিরাপত্তা সীমার নিচে (Low Stock Alert!)",
        "stock_out" to "মোট খরচ বা ডেলিভারি",
        "stock_in" to "মোট গৃহীত বা আমদানি",
        "reserved_stock" to "সংরক্ষিত বরাদ্দ",
        "damaged_stock" to "ক্ষতিগ্রস্ত স্টক",
        "value" to "হিসাবকৃত মজুদ পণ্যমূল্য",
        "search_placeholder" to "নাম, কোড, লট অথবা সেকশন দিয়ে খুঁজুন...",
        "no_products" to "ডাটাবেজে কোনো পণ্যের তথ্য পাওয়া যায়নি।",
        
        // Consumption Registry
        "log_consumption" to "দৈনিক ব্যবহারের তালিকাভুক্তি",
        "qty_used" to "ব্যবহৃত পরিমাণ (Unit অনুযায়ী)",
        "dept" to "ডাইং / ব্যবহারকারী ডিপার্টমেন্ট",
        "operator" to "স্টোর প্রদানকারী কর্মকর্তা",
        "notes" to "ব্যবহার সক্রান্ত নোট/মন্তব্য",
        "consumption_log" to "দৈনিক পণ্য ব্যবহারের খতিয়ান ও লগবুক",
        "save_consumption" to "পণ্য বিতরণ সম্পন্ন করুন",
        
        // Purchase Panel
        "log_purchase" to "নতুন কাঁচামাল বা লট রিসিভ করুন",
        "supplier" to "পাইকারি সরবরাহকারী",
        "invoice" to "চালান বা বিল রেফারেন্স বাটন",
        "purchase_log" to "ক্রয়কৃত পণ্যের ঐতিহাসিক হিস্ট্রি লগ",
        
        // Supplier Panel
        "add_supplier" to "সরবরাহকারী ডাটাবেজে যুক্ত করুন",
        "supplier_mobile" to "মোবাইল বা কন্টাক্ট নাম্বার",
        "supplier_addr" to "সরবরাহকারী প্রতিষ্ঠানের ঠিকানা",
        
        // Export & Reports
        "reports_header" to "কারখানা ইনভেন্টরি রিপোর্টস সেন্টার",
        "export_hint" to "প্রয়োজনীয় ডাটার এক্সপোর্ট ফাইল লোকাল মেমোরিতে জেনারেট করুন:",
        "export_pdf" to "পিডিএফ ফাইল এক্সপোর্ট (PDF)",
        "export_excel" to "এক্সেল ফাইল এক্সপোর্ট (Excel)",
        "export_csv" to "সিএসভি ফাইল এক্সপোর্ট (CSV)",
        "backup_restore" to "ডাটা ব্যাকআপ ও রিস্টোর (SQLite Storage)",
        "backup_db" to "ডাটাবেজ ব্যাকআপ নিন",
        "restore_db" to "ডাটাবেজ রিস্টোর করুন",
        "export_success" to "এক্সপোর্ট সম্পন্ন হয়েছে! ফাইল লোকাল স্টোরেজে ড্রাইভে প্রস্তুত।",
        
        // Security Mode
        "roles_title" to "ব্যবহারকারীর সিকিউরিটি রোলস",
        "current_role" to "বর্তমানে নির্বাচিত রোল",
        "btn_admin" to "এডমিন (সর্বোচ্চ প্রবেশাধিকার)",
        "btn_manager" to "ম্যানেজার (ড্যাশবোর্ড ও রিপোর্টস)",
        "btn_keeper" to "স্টোর কিপার (বিতরণ ও এন্ট্রি)",
        "btn_viewer" to "ভিউয়ার (শুধুমাত্র দেখার অ্যাক্সেস)"
    )
}
