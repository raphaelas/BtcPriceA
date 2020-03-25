package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.btcpricea.myapplication.CurrencyListingsJsonOuter
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.text.DecimalFormat
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    private fun fetchFirstCurrencyListing(): CurrencyListingsJsonOuter {
        val url1 = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?limit=1"
        val urlConnection1: HttpsURLConnection = URL(url1).openConnection() as HttpsURLConnection
        urlConnection1.setRequestProperty("X-CMC_PRO_API_KEY", " b295ec78-ec7d-4be4-8ff3-46d7d8d62cb7")
        val inputStream1: InputStream = urlConnection1.inputStream
        val reader1 = InputStreamReader(inputStream1, "UTF-8")
        return Gson().fromJson(reader1, CurrencyListingsJsonOuter::class.java)
    }

    private fun fetchSecondCurrencyListing(): CurrencyListingsJsonOuter {
        val url2 = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=2&limit=1&convert=BTC"
        val urlConnection2: HttpsURLConnection = URL(url2).openConnection() as HttpsURLConnection
        urlConnection2.setRequestProperty("X-CMC_PRO_API_KEY", " b295ec78-ec7d-4be4-8ff3-46d7d8d62cb7")
        val inputStream2: InputStream = urlConnection2.inputStream
        val reader2 = InputStreamReader(inputStream2, "UTF-8")
        return Gson().fromJson(reader2, CurrencyListingsJsonOuter::class.java)
    }

    private fun showCurrencyListings(parsedJson1: CurrencyListingsJsonOuter, parsedJson2: CurrencyListingsJsonOuter) {
        val symbolLabel1View = findViewById < TextView >(R.id.symbol_label1)
        val price1View = findViewById<TextView>(R.id.price1)

        val symbol1 = parsedJson1.data.first().symbol
        val price1 = parsedJson1.data.first().quote.USD.price
        symbolLabel1View.text = String.format("%s -> USD", symbol1)
        price1View.text = String.format("$%s", price1)

        val symbolLabel2View = findViewById<TextView>(R.id.symbol_label2)
        val price2View = findViewById<TextView>(R.id.price2)

        val symbol2 = parsedJson2.data.first().symbol
        val price2 = parsedJson2.data.first().quote.BTC.price
        symbolLabel2View.text = String.format("%s -> %s", symbol1, symbol2)
        price2View.text = DecimalFormat("#0.00000000").format(price2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_main)
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e("RepairableException", "Google Play Services could have been updated.")
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e("SecurityException", "Google Play Services not available.")
        }
        launch {
            val parsedJson1 = async(Dispatchers.IO) { fetchFirstCurrencyListing() }
            val parsedJson2 = async(Dispatchers.IO) { fetchSecondCurrencyListing() }
            showCurrencyListings(parsedJson1.await(), parsedJson2.await())
        }
        val refreshButton = findViewById<Button>(R.id.refresh_button)
        refreshButton.setOnClickListener {
            launch {
                val parsedJson1 = async(Dispatchers.IO) { fetchFirstCurrencyListing() }
                val parsedJson2 = async(Dispatchers.IO) { fetchSecondCurrencyListing() }
                showCurrencyListings(parsedJson1.await(), parsedJson2.await())
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
