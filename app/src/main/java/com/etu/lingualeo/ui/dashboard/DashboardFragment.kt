package com.etu.lingualeo.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.etu.lingualeo.R
import com.etu.lingualeo.RandomString
import com.etu.lingualeo.textEditor.TextEditorActivity
import com.etu.lingualeo.wordSelector.WordSelectorActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom


const val PERMISSION_CAMERA = 1
const val PERMISSION_GALLERY = 2

const val RESULT_CAMERA = 1

class DashboardFragment : Fragment() {

    private lateinit var fileName: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val status = checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (status == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            fileName = prepareFile()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileName)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, RESULT_CAMERA)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA)
        }

//        root.findViewById<Button>(R.id.button).setOnClickListener {
//            startActivity(Intent(activity, WordSelectorActivity::class.java))
//}

        return root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CAMERA -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fileName = prepareFile()
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileName)
                startActivityForResult(intent, RESULT_CAMERA)
            }
        }
    }

    fun prepareFile(): Uri {
        val outputDir = context?.externalCacheDir
        val outputFile = FileProvider.getUriForFile(
            context!!,
            context!!.getApplicationContext().getPackageName() + ".p_provider",
            File.createTempFile(RandomString(12).nextString(), ".jpg", outputDir)
        );
        Log.i("file", outputFile.toString())
        return outputFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK) {
            Handler().postDelayed({
                val picture = MediaStore.Images.Media.getBitmap(context?.contentResolver, fileName);
                val text = getTextFromImage(picture)
                val intent = Intent(activity, TextEditorActivity::class.java)
                intent.putExtra("text", text)
                startActivity(intent)
                val a: View = activity!!.findViewById(R.id.navigation_home)
                Handler().postDelayed({
                    a.performClick()
                }, 100)
            }, 100)
        }
    }

    fun getTextFromImage(bitmap: Bitmap): String {
        val textRecognizer = TextRecognizer.Builder(context).build()
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val items = textRecognizer.detect(frame)
        val sb = StringBuilder()
        for (i in 0 until items.size()) {
            sb.append(items.valueAt(i).value)
            sb.append("\n")
        }
        Log.i("aa", sb.toString())
        return sb.toString()
    }
}
