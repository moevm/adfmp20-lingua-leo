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
import android.view.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.etu.lingualeo.R
import com.etu.lingualeo.RandomString
import com.etu.lingualeo.textEditor.TextEditorActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.File


const val PERMISSION_CAMERA = 1

const val RESULT_CAMERA = 1

class DashboardFragment : Fragment() {

    private lateinit var fileName: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        Handler().postDelayed({
            activity!!.runOnUiThread {
                registerForContextMenu(button)
                button.performLongClick()
            }
        }, 30)
        return root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        when(item.toString()) {
            "Камера" -> checkCameraPermissionsAndRun()
            "Галерея" -> startGalleryActivity()
            "Текст" -> startEditTextActivity(null)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CAMERA -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPicture()
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, v.id, 0, "Камера")
        menu.add(0, v.id, 0, "Галерея")
        menu.add(0, v.id, 0, "Текст")
    }

    fun getPicture() {
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        fileName = prepareFile()
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, fileName)
        intentCamera.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intentCamera, RESULT_CAMERA)
    }

    fun prepareFile(): Uri {
        val outputDir = context?.externalCacheDir
        val outputFile = FileProvider.getUriForFile(
            context!!,
            context!!.getApplicationContext().getPackageName() + ".p_provider",
            File.createTempFile(RandomString(12).nextString(), ".jpg", outputDir)
        );
        return outputFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data?.data != null) fileName = data.data!!
            val picture = MediaStore.Images.Media.getBitmap(context?.contentResolver, fileName);
            val text = getTextFromImage(picture)
            startEditTextActivity(text)
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
        return sb.toString()
    }

    fun startGalleryActivity() {
        val intentGallery = Intent(Intent.ACTION_GET_CONTENT);
        intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
        intentGallery.setType("image/*")
        startActivityForResult(intentGallery, RESULT_CAMERA)
    }

    fun startEditTextActivity(text: String?) {
        val intent = Intent(activity, TextEditorActivity::class.java)
        if(text != null) intent.putExtra("text", text)
        startActivity(intent)
        val a: View = activity!!.findViewById(R.id.navigation_home)
        Handler().postDelayed({
            a.performClick()
        }, 100)
    }

    fun checkCameraPermissionsAndRun() {
        val status = checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if(status == PackageManager.PERMISSION_GRANTED) {
            getPicture()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA)
        }
    }
}
