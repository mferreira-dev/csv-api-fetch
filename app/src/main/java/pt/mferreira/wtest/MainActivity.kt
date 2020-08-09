package pt.mferreira.wtest

import adapters.ZipCodeAdapter
import android.Manifest
import android.app.DownloadManager
import android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import models.ZipCode
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var context: Context
    private var zipCodes: MutableList<ZipCode> = ArrayList()
    private val zipCodeAdapter = ZipCodeAdapter(this, zipCodes)
    lateinit var suggestionsAdapter: SimpleCursorAdapter
    private var history = arrayListOf<String>()
    lateinit var searchView: SearchView
    lateinit var currentCursor: Cursor
    var interrupted = false

    inner class Importer : AsyncTask<Void, Void, Int>() {
        private var progressDialog = ProgressDialog(context)

        override fun onPreExecute() {
            super.onPreExecute()

            if (interrupted) {
                progressDialog.setMessage("The app was interrupted while setting up. Please wait while we try again...")
                progressDialog.setCancelable(false)
                progressDialog.show()
            } else {
                progressDialog.setMessage("Please wait while data is being imported...")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            progressDialog.dismiss()
            setupRecyclerView()

            // Delete CSV file (no longer needed).
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val files = directory.listFiles()

            for (file in files) {
                if (file.name == "codigos_postais.csv")
                    file.delete()
            }
        }

        override fun doInBackground(vararg params: Void?): Int {
            val db: SQLiteDatabase = context.openOrCreateDatabase("csv.db", 0, null)

            var reader: BufferedReader? = null
            var sql = ""

            try {
                reader = BufferedReader(FileReader("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/codigos_postais.csv"))

                // Create table if it does not exist.
                sql = "CREATE TABLE IF NOT EXISTS info (id INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1"
                var line = reader.readLine()
                val columns = line.split(",")

                // Build table creation string.
                for (column in columns)
                    sql = "$sql, $column VARCHAR"
                sql = "$sql);"

                // Create database and table.
                db.execSQL(sql)

                // Read data from CSV file and insert into the database while
                // skipping problematic entries (those who contain quotes).
                line = reader.readLine()
                while (line.contains("\"")) line = reader.readLine()

                // Keep reading until EOF.
                db.beginTransaction();
                while (line != null) {
                    // Separate data to be inserted.
                    var contentValues = ContentValues()
                    var separated = line.split(",")

                    for (i in columns.indices) contentValues.put(columns[i], separated[i])

                    // Execute.
                    var result = db.insert("info", null, contentValues)

                    // Keep skipping problematic entries.
                    line = reader.readLine()
                    while (line.contains("\"")) line = reader.readLine()
                }
            } catch (ex: Exception) {
                System.out.println(ex)
            }

            // Close database properly.
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close()

            // Add flag for setup complete.
            val fos: FileOutputStream = openFileOutput("setup.txt", MODE_APPEND)
            fos.write("true".toByteArray())
            fos.close()

            return 1
        }
    }

    inner class PopulateAdapterFromCursor : AsyncTask<Cursor, Void, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Cursor): Int? {
            lateinit var c: Cursor

            if (params.isNotEmpty()) {
                c = params[0]
                currentCursor = c
            }

            // Load appropriate entries.
            // If there are less than 1000 entries, load all of them.
            // Otherwise load in increments of 1000.
            if (currentCursor.count < 1000) {
                currentCursor.moveToFirst()
                do saveZipCode(currentCursor) while (currentCursor.moveToNext())
            } else {
                if (zipCodes.size == 0) {
                    currentCursor.moveToFirst()

                    do {
                        saveZipCode(currentCursor)
                    } while (currentCursor.moveToNext() && zipCodes.size < 1000)
                } else {
                    var newSize = zipCodes.size + 1000
                    currentCursor.move(zipCodes.size)

                    do {
                        saveZipCode(currentCursor)
                    } while (currentCursor.moveToNext() && zipCodes.size < newSize)
                }
            }

            return 1
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            changeLoadState(false)
            zipCodeAdapter.notifyDataSetChanged()
        }
    }

    private fun saveZipCode(c: Cursor) {
        zipCodes.add(
            ZipCode(
                c.getString(1),
                c.getString(2),
                c.getString(3),
                c.getString(4),
                c.getString(5),
                c.getString(6),
                c.getString(7),
                c.getString(8),
                c.getString(9),
                c.getString(10),
                c.getString(11),
                c.getString(12),
                c.getString(13),
                c.getString(14),
                c.getString(15),
                c.getString(16),
                c.getString(17)
            )
        )
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_DOWNLOAD_COMPLETE -> {
                    Importer().execute()
                }
            }
        }
    }

    // Download from GitHub repo.
    private fun download() {
        // Check if CSV file already exists. If not, download it.
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = directory.listFiles()

        var names = arrayListOf<String>()
        for (file in files) names.add(file.name)

        var line = ""
        try {
            val fis: FileInputStream = openFileInput("setup.txt")
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            line = br.readLine()

            line = br.readLine()

            isr.close()
            fis.close()
        } catch (ex: Exception) {
            // File does not exist yet.
        }

        var flag = line == "true"

        if (!names.contains("codigos_postais.csv") && !flag && isNetworkAvailable()) {
            val url = "https://raw.githubusercontent.com/centraldedados/codigos_postais/master/data/codigos_postais.csv"
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("WTest").setDescription("Downloading CSV file...")

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "codigos_postais.csv")

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        } else if (!names.contains("codigos_postais.csv") && !flag && !isNetworkAvailable()) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage("Please try again after connecting to the Internet.")
            builder.setCancelable(true)

            builder.setPositiveButton("OK") { dialog, id ->
                finish();
            }

            val alert: AlertDialog = builder.create()
            alert.show()
        } else if (names.contains("codigos_postais.csv") && !flag) {
            interrupted = true
            Importer().execute()
        }
    }

    private fun doesDatabaseExist(name: String): Boolean {
        val file: File = context.getDatabasePath(name)
        return file.exists()
    }

    private fun setupRecyclerView () {
        // Convert data from the database to POJOs.
        if (doesDatabaseExist("csv.db")) {
            var sql = "SELECT * FROM info"
            val db = openOrCreateDatabase("csv.db", 0, null)
            val c: Cursor = db.rawQuery(sql, null)

            PopulateAdapterFromCursor().execute(c)
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        zipCodeRecyclerView.layoutManager = layoutManager

        // Load more entries when the user scrolls all the way to the bottom of the RecyclerView.
        zipCodeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    // LOAD MOAR
                    if (!noResultsLayout.isVisible)
                        PopulateAdapterFromCursor().execute()
                }
            }
        })

        zipCodeRecyclerView.adapter = zipCodeAdapter
    }

    private fun changeLoadState (state: Boolean) {
        if (state) progressZipCodes.visibility = View.VISIBLE
        else progressZipCodes.visibility = View.INVISIBLE
    }

    // TODO: Refactor MainActivity into a fragment and use string resources instead of hardcoding them.
    // TODO: Add documentation to methods and functions.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        changeLoadState(true)
        zipCodes.clear()

        // Setup toolbar and drawer stuff.
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer, R.string.navigation_close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        registerReceiver(broadcastReceiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
        setupRecyclerView()

        if (!doesDatabaseExist("csv.db")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                } else
                    download()
            } else
                download()
        }

        if (savedInstanceState == null)
            navigationView.setCheckedItem(R.id.navZipCode)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    // Load input history from file.
    private fun loadHistory() {
        history = arrayListOf()

        try {
            val fis: FileInputStream = openFileInput("history.txt")
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            var line = br.readLine()

            while (line != null) {
                history.add(line)
                line = br.readLine()
            }

            isr.close()
            fis.close()
        } catch (ex: Exception) {
            // File does not exist yet.
        }

        history.reverse()

        val from = arrayOf("string")
        val to = intArrayOf(android.R.id.text1)
        suggestionsAdapter = SimpleCursorAdapter(context, R.layout.suggestion_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)

        updateSuggestionsAdapter("")
        suggestionsAdapter.notifyDataSetChanged()
        searchView.suggestionsAdapter = suggestionsAdapter
    }

    // Update suggestions as user modifies the field.
    private fun updateSuggestionsAdapter(s: String) {
        val c = MatrixCursor(arrayOf(BaseColumns._ID, "string"))
        val list = mutableListOf<String>()

        if (s.isNotEmpty()) {
            for (entry in history) if (entry.contains(s)) list.add(entry)
        } else {
            for (entry in history) list.add(entry)
        }

        for (i in 0 until list.size) c.addRow(arrayOf(i, list[i]))
        suggestionsAdapter.changeCursor(c)
    }

    // Prepare strings such as "0000-000" and "0000 000" for database operations.
    private fun separateBasedOn(ch: Char, s: String): List<String> {
        var str = ""
        for (c in s)  {
            if (c == ch)
                str = "$str$ch"
        }
        return s.split(str)
    }

    // Prepare location string from SearchView for database operations.
    private fun formatLocation(location: String): String {
        var list = mutableListOf<String>()

        var matcher = Pattern.compile("[A-zÀ-ú]*").matcher(location.toLowerCase())
        while (matcher.find())  {
            list.add("%${matcher.group(0)}%")
        }

        list.removeAll(listOf("%%"))
        var formatted = ""
        for (i in 0 until list.size) {
            if (list[i] == "")
                formatted = "$formatted "
            else
                formatted = "$formatted${list[i]}"
        }
        formatted = formatted.replace("%%", "%")

        return "'$formatted'"
    }

    private fun queryZipCodes(db: SQLiteDatabase, main: String, ext: String): Cursor {
        lateinit var results: Cursor

        if (ext.isNotEmpty() && main.isNotEmpty())
            results = db.query("info", null, "num_cod_postal LIKE $main AND ext_cod_postal LIKE $ext",
            null, null, null, null)
        else if (ext.isEmpty())
            results = db.query("info", null, "num_cod_postal LIKE $main",
                null, null, null, null)
        else if (main.isEmpty() && ext.isNotEmpty())
            results = db.query("info", null, "ext_cod_postal LIKE $ext",
                null, null, null, null)

        if (results.count == 0) noResults(true)
        else noResults(false)

        return results
    }

    // Replace vowels with underscores. (Diacritics workaround)
    private fun queryLocations(db: SQLiteDatabase, location: String): Cursor {
        val vowels = "aáàâãeéèiíìoóòôuúùAÁÀÂÃEÉÈIÍÌOÓÒÔUÚÙ"
        var newLocation = ""

        for (i in location.indices) {
            if (vowels.contains(location[i])) newLocation = "${newLocation}_"
            else newLocation = "$newLocation${location[i]}"
        }

        var results =  db.query("info", null, "nome_localidade LIKE $newLocation OR desig_postal LIKE $newLocation",
            null, null, null, null)

        if (results.count == 0) noResults(true)
        else noResults(false)

        return results
    }

    private fun queryZipCodesAndLocations(db: SQLiteDatabase, location: String, main: String, ext: String): Cursor {
        lateinit var results: Cursor
        val vowels = "aáàâãeéèiíìoóòôuúùAÁÀÂÃEÉÈIÍÌOÓÒÔUÚÙ"
        var newLocation = ""

        for (i in location.indices) {
            if (vowels.contains(location[i])) newLocation = "${newLocation}_"
            else newLocation = "$newLocation${location[i]}"
        }

        if (ext.isNotEmpty() && main.isNotEmpty())
            results = db.query("info", null,
                "nome_localidade LIKE $newLocation OR desig_postal LIKE $newLocation AND num_cod_postal LIKE $main AND ext_cod_postal LIKE $ext",
                null, null, null, null)
        else if (ext.isEmpty() && main.isNotEmpty())
            results = db.query("info", null,
                "nome_localidade LIKE $newLocation OR desig_postal LIKE $newLocation AND num_cod_postal LIKE $main",
                null, null, null, null)
        else if (main.isEmpty() && ext.isNotEmpty())
            results = db.query("info", null,
                "nome_localidade LIKE $newLocation OR desig_postal LIKE $newLocation AND ext_cod_postal LIKE $ext",
                null, null, null, null)

        if (results.count == 0) noResults(true)
        else noResults(false)

        return results
    }

    private fun noResults(a: Boolean) {
        if (a) noResultsLayout.visibility = View.VISIBLE
        else noResultsLayout.visibility = View.INVISIBLE
    }

    private fun separateWordNumberWord(s: String): List<String> {
        var results = mutableListOf<String>()

        results.add("")
        results.add("")
        results.add("")

        var flag = false
        for (i in s.indices) {
            if (s[i] != ' ' && !s[i].isDigit()) {
                if (!flag) results[0] = "${results[0]}${s[i]}"
                else results[2] = "${results[2]}${s[i]}"
            } else {
                flag = true
                results[1] = "${results[1]}${s[i]}"
            }
        }

        for (i in results.indices) results[i] = results[i].trim()
        return results
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                val capabilities: NetworkCapabilities? = cm.getNetworkCapabilities(cm.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> { return true }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> { return true }
                    }
                }
            }
        } else {
            if (cm != null) {
                val activeNetwork = cm.activeNetworkInfo
                if (activeNetwork != null) {
                    if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) return true
                    else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return true
                }
            }
        }

        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    download()
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setMessage("This app requires permission to write to the device's storage. It cannot properly function otherwise.")
                    builder.setCancelable(true)

                    builder.setPositiveButton("I understand") { dialog, id ->
                        finish();
                    }

                    val alert: AlertDialog = builder.create()
                    alert.show()
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView

        val autoCompleteTextViewID = resources.getIdentifier("search_src_text", "id", packageName)
        val searchAutoCompleteTextView = searchView.findViewById(autoCompleteTextViewID) as? AutoCompleteTextView
        searchAutoCompleteTextView?.threshold = 0

        val dropDownAnchor: View = searchView.findViewById(searchAutoCompleteTextView!!.dropDownAnchor);
        dropDownAnchor.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> // screen width
            val screenWidthPixel: Int = resources.displayMetrics.widthPixels
            searchAutoCompleteTextView.dropDownWidth = screenWidthPixel
        }

        loadHistory()

        searchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionsAdapter.getItem(position) as Cursor
                val txt = cursor.getString(cursor.getColumnIndex("string"))
                searchView.setQuery(txt, true)

                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }
        })

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                val fos: FileOutputStream = openFileOutput("history.txt", MODE_APPEND)
                fos.write("$s\n".toByteArray())
                fos.close()
                loadHistory()

                // Search database.
                lateinit var matcher: Matcher
                val db: SQLiteDatabase = context.openOrCreateDatabase("csv.db", 0, null)

                when {
                    Pattern.compile("[A-zÀ-ú]+\\s+\\d{3}\\d*\\s+[A-zÀ-ú]+").matcher(s.toLowerCase()).find() -> {
                        // Word number word.
                        //separate them and if the number is 4 or more it's a main
                        matcher = Pattern.compile("[A-zÀ-ú]+\\s+\\d{3}\\d*\\s+[A-zÀ-ú]+").matcher(s)
                        matcher.find()

                        val sep = separateWordNumberWord(matcher.group(0))
                        val location = "'${sep[0]} ${sep[2]}'"
                        val formatted = formatLocation(location.toLowerCase())

                        zipCodes.clear()
                        when {
                            sep[1].length == 3 -> {
                                val results = queryZipCodesAndLocations(db, formatted, "", sep[1])
                                if (results.count > 0)
                                    PopulateAdapterFromCursor().execute(results)
                            }
                            sep[1].length == 4 -> {
                                val results = queryZipCodesAndLocations(db, formatted, sep[1], "")
                                if (results.count > 0)
                                    PopulateAdapterFromCursor().execute(results)
                            }
                            sep[1].length > 4 -> {
                                var results = queryZipCodesAndLocations(db, formatted, sep[1].substring(0, 4), sep[1].substring(4))
                                if (results.count > 0)
                                    PopulateAdapterFromCursor().execute(results)
                            }
                        }
                    }
                    Pattern.compile("\\d{4}-+\\d{3}").matcher(s).find() -> {
                        // Standard zip Code.
                        val sep = separateBasedOn('-', s)

                        zipCodes.clear()
                        val results = queryZipCodes(db, sep[0], sep[1])
                        if (results.count > 0)
                            PopulateAdapterFromCursor().execute(results)
                    }
                    Pattern.compile("\\d{4}\\s+\\d{3}").matcher(s).find() -> {
                        // Spaced zip code.
                        matcher = Pattern.compile("\\d{4}\\s+\\d{3}").matcher(s)
                        matcher.find()
                        var sep = separateBasedOn(' ', matcher.group(0))

                        zipCodes.clear()
                        val results = queryZipCodes(db, sep[0], sep[1])
                        if (results.count > 0)
                            PopulateAdapterFromCursor().execute(results)
                    }
                    Pattern.compile("\\d{4}\\d{3}").matcher(s).find() -> {
                        // Glued zip code.
                        matcher = Pattern.compile("\\d{4}\\d{3}").matcher(s)
                        matcher.find()

                        zipCodes.clear()
                        val results = queryZipCodes(db, matcher.group(0).substring(0, 4), matcher.group(0).substring(4))
                        if (results.count > 0)
                            PopulateAdapterFromCursor().execute(results)
                    }
                    Pattern.compile("\\d{4}\\.*").matcher(s).find() -> {
                        // Main code only.
                        matcher = Pattern.compile("\\d{4}\\.*").matcher(s)
                        matcher.find()

                        zipCodes.clear()
                        val results = queryZipCodes(db, matcher.group(0), "")
                        if (results.count > 0)
                            PopulateAdapterFromCursor().execute(results)
                    }
                    Pattern.compile("[A-zÀ-ú]+\\b").matcher(s.toLowerCase()).find() -> {
                        // Location.
                        val formatted = formatLocation(s.toLowerCase())
                        zipCodes.clear()
                        val results = queryLocations(db, formatted)
                        if (results.count > 0)
                            PopulateAdapterFromCursor().execute(results)
                    }
                }

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                updateSuggestionsAdapter(s)
                return false
            }
        })

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navZipCode -> {
                navigationView.setCheckedItem(R.id.navZipCode)
            }
            R.id.navArticle -> {
                navigationView.setCheckedItem(R.id.navArticle)
                val intent = Intent(applicationContext, ArticlesActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}