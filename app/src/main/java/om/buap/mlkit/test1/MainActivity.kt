package om.buap.mlkit.test1

import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.scd.MLSceneDetection
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var analyzer: MLSceneDetectionAnalyzer? = null
    private var textView: TextView? = null
    private var imageView: ImageView? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.scene_detect).setOnClickListener(this)
        findViewById<View>(R.id.selectImage).setOnClickListener(View.OnClickListener {
            //Toast.makeText(this.applicationContext, "Select Image", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 111)
        })
        textView = findViewById(R.id.result_scene)
        imageView = findViewById(R.id.imageSelect)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
                imageView?.setImageBitmap(bitmap)
               // imageView?.setImageURI(data?.data)

            } catch (e:IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(applicationContext, "Error loading image", Toast.LENGTH_LONG)
        }

    }

    override fun onClick(v: View?) {
        this.analyzer()
    }

    private fun analyzer() {
        analyzer = MLSceneDetectionAnalyzerFactory.getInstance().sceneDetectionAnalyzer
        // Create an MLFrame using android.graphics.Bitmap. Recommended image size: large than 224*224.
        //val originBitmap =  BitmapFactory.decodeResource(this.resources, R.drawable.train1)
        val frame = MLFrame.Creator()
            .setBitmap(bitmap)
            .create()
        val task = analyzer!!.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { sceneInfos ->
            if (sceneInfos != null && !sceneInfos.isEmpty()) {
                this@MainActivity.displaySuccess(sceneInfos)
            } else {
                this@MainActivity.displayFailure()
            }
        }.addOnFailureListener { this@MainActivity.displayFailure() }
    }

    private fun displaySuccess(sceneInfos: List<MLSceneDetection>) {
        var str = """
        Scene Count：${sceneInfos.size}
       
        """.trimIndent()
        for (i in sceneInfos.indices) {
            val sceneInfo = sceneInfos[i]
            str += """
            Scene：${sceneInfo.result}
            Confidence：${sceneInfo.confidence}
            
            """.trimIndent()
        }
        textView!!.text = str
    }

    private fun displayFailure() {
        Toast.makeText(this.applicationContext, "Detection Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (analyzer != null) {
            analyzer!!.stop()
        }
    }

}