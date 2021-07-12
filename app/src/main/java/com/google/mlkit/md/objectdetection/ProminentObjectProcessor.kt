/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.md.objectdetection

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import android.os.Environment
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.graphics.toRect
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.md.InputInfo
import com.google.mlkit.md.R
import com.google.mlkit.md.camera.CameraReticleAnimator
import com.google.mlkit.md.camera.FrameProcessorBase
import com.google.mlkit.md.camera.GraphicOverlay
import com.google.mlkit.md.camera.WorkflowModel
import com.google.mlkit.md.camera.WorkflowModel.WorkflowState
import com.google.mlkit.md.settings.PreferenceUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import org.buyun.credit.sdk.CreditResult
import org.buyun.credit.sdk.CreditSDK
import org.buyun.credit.sdk.SDK_IMAGE_TYPE
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.util.*
import java.util.concurrent.Callable


/** A processor to run object detector in prominent object only mode.  */
class ProminentObjectProcessor(
    private val context: Context,
    graphicOverlay: GraphicOverlay,
    private val workflowModel: WorkflowModel,
    private val customModelPath: String) :
    FrameProcessorBase<List<DetectedObject>>() {

    private val confirmationController: ObjectConfirmationController = ObjectConfirmationController(graphicOverlay)
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)
    private val reticleOuterRingRadius: Int = graphicOverlay
            .resources
            .getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_radius)

    internal class Field {
        var name: String? = null
        var value: String? = null
        var confidence = 0f
        var isValid = false
    }


    internal class Card {
        var isNumberValid = false
        var detectionConfidence = 0f
        var recognitionMinConfidence = 100f
        var warpedBox: FloatArray?= null
        var fields: MutableList<Field>? = null
    }

    init {
        // Init the engine
        val result: CreditResult? = CreditSDK.init(context.getAssets())
    }

    override fun stop() {
        super.stop()
        try {
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close object detector!", e)
        }
    }


    private fun extractCards(result: CreditResult): List<Card> {
        val cards: MutableList<Card> =
            LinkedList<Card>()
        if (!result.isOK || result.numCards() == 0L) {
            return cards
        }
        val jsonString = result.json()
            ?: // No card
            return cards
        try {
            val jObject = JSONObject(jsonString)
            if (jObject.has("cards") && !jObject.isNull("cards")) {
                val jCards = jObject.getJSONArray("cards")
                for (i in 0 until jCards.length()) {
                    val jCard = jCards.getJSONObject(i)
                    val jWarpedBox = jCard.getJSONArray("warpedBox")
                    val card: Card = Card()
                    card.fields = LinkedList<Field>()
                    card.warpedBox = FloatArray(8)
                    for (j in 0..7) {
                        card.warpedBox!![j] = jWarpedBox.getDouble(j).toFloat()
                    }
                    card.detectionConfidence = jCard.getDouble("confidence").toFloat()
                    card.recognitionMinConfidence = 100f
                    card.isNumberValid = false
                    if (jCard.has("fields") && !jCard.isNull("fields")) {
                        val jFields = jCard.getJSONArray("fields")
                        for (j in 0 until jFields.length()) {
                            val jField = jFields.getJSONObject(j)
                            val field: Field =
                                Field()
                            field.confidence = jField.getDouble("confidence").toFloat()
                            field.name = jField.getString("name")
                            field.value = jField.getString("value")
                            if (jField.has("valid")) {
                                field.isValid = jField.getBoolean("valid")
                            }
                            card.recognitionMinConfidence = Math.min(card.recognitionMinConfidence, field.confidence)
                            card.isNumberValid = card.isNumberValid or (field.name == "number" && field.isValid)
                            card.fields!!.add(field)
                        }
                    }
                    cards.add(card)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, e.toString())
        }
        return cards
    }

    override fun detectInImage(image: InputImage): Task<List<DetectedObject>> {
        return Tasks.call(
            THREAD_POOL_EXECUTOR,
            Callable<List<DetectedObject>> {

                var results: MutableList<DetectedObject> = ArrayList()

                try {
                    val out = ByteArrayOutputStream()
                    val yuvImage = YuvImage(image.byteBuffer.array(), image.format, image.width, image.height, null)
                    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
                    val imageBytes = out.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    val matrix = Matrix()
                    matrix.postRotate(image.rotationDegrees.toFloat())

                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        matrix,
                        true
                    )

                    val widthInBytes = rotatedBitmap.rowBytes
                    val width = rotatedBitmap.width
                    val height = rotatedBitmap.height
                    val nativeBuffer = ByteBuffer.allocateDirect(widthInBytes * height)
                    rotatedBitmap.copyPixelsToBuffer(nativeBuffer)
                    nativeBuffer.rewind()

                    /*marked important code*/

                    var i: Int = 0
                    for(card in cardList) {
                        val borderWidth: Float = 0.0f
                        // Transform corners
                        val warpedBox: FloatArray = card.warpedBox!!
                        val cornerA = PointF(
                            warpedBox[0] - borderWidth,
                            warpedBox[1] - borderWidth
                        )
                        val cornerB = PointF(
                            warpedBox[2] + borderWidth,
                            warpedBox[3] - borderWidth
                        )
                        val cornerC = PointF(
                            warpedBox[4] + borderWidth,
                            warpedBox[5] + borderWidth
                        )
                        val cornerD = PointF(
                            warpedBox[6] - borderWidth,
                            warpedBox[7] + borderWidth
                        )

                        // Draw border
                        val pathBorder = Path()
                        pathBorder.moveTo(cornerA.x, cornerA.y)
                        pathBorder.lineTo(cornerB.x, cornerB.y)
                        pathBorder.lineTo(cornerC.x, cornerC.y)
                        pathBorder.lineTo(cornerD.x, cornerD.y)
                        pathBorder.lineTo(cornerA.x, cornerA.y)
                        pathBorder.close()

                        val rectF = RectF()
                        pathBorder.computeBounds(rectF, true)
                        if(rectF.left < 0)
                            rectF.left = 0.0f

                        if(rectF.top < 0)
                            rectF.top = 0.0f

                        if(rectF.right >= width)
                            rectF.right = (width - 1).toFloat()

                        if(rectF.bottom >= height)
                            rectF.bottom = (height - 1).toFloat()

                        val labels : MutableList<DetectedObject.Label> = ArrayList()
                        val itr = card.fields!!.listIterator()
                        var label: String = ""
                        while(itr.hasNext()) {
                            val field = itr.next()
                            label += field.value;
                            label += "\n"
                        }

                        labels.add(DetectedObject.Label(label, 0.toFloat(), 0))

                        val detectObj = DetectedObject(rectF.toRect(), i, labels)
                        results.add(detectObj)
                        i ++
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }


                results
            })
    }

    @MainThread
    override fun onSuccess(
        inputInfo: InputInfo,
        results: List<DetectedObject>,
        graphicOverlay: GraphicOverlay
    ) {
        var objects = results
        if (!workflowModel.isCameraLive) {
            return
        }

        if (PreferenceUtils.isClassificationEnabled(graphicOverlay.context)) {
            val qualifiedObjects = ArrayList<DetectedObject>()
            qualifiedObjects.addAll(objects)
            objects = qualifiedObjects
        }

        val objectIndex = 0
        val hasValidObjects = objects.isNotEmpty() &&
            (customModelPath == null || DetectedObjectInfo.hasValidLabels(objects[objectIndex]))
        if (!hasValidObjects) {
            confirmationController.reset()
            workflowModel.setWorkflowState(WorkflowState.DETECTING)
        } else {
            val visionObject = objects[objectIndex]
            if (objectBoxOverlapsConfirmationReticle(graphicOverlay, visionObject)) {
                // User is confirming the object selection.
                confirmationController.confirming(visionObject.trackingId)
                workflowModel.confirmingObject(
                        DetectedObjectInfo(visionObject, objectIndex, inputInfo), confirmationController.progress
                )
            } else {
                // Object detected but user doesn't want to pick this one.
                confirmationController.reset()
                workflowModel.setWorkflowState(WorkflowState.DETECTED)
            }
        }

        graphicOverlay.clear()
        if (!hasValidObjects) {
            graphicOverlay.add(ObjectReticleGraphic(graphicOverlay, cameraReticleAnimator))
            cameraReticleAnimator.start()
        } else {
            if (objectBoxOverlapsConfirmationReticle(graphicOverlay, objects[0])) {
                // User is confirming the object selection.
                cameraReticleAnimator.cancel()
                graphicOverlay.add(
                        ObjectGraphicInProminentMode(
                                graphicOverlay, objects[0], confirmationController
                        )
                )
                if (!confirmationController.isConfirmed &&
                    PreferenceUtils.isAutoSearchEnabled(graphicOverlay.context)) {
                    // Shows a loading indicator to visualize the confirming progress if in auto search mode.
                    graphicOverlay.add(ObjectConfirmationGraphic(graphicOverlay, confirmationController))
                }
            } else {
                // Object is detected but the confirmation reticle is moved off the object box, which
                // indicates user is not trying to pick this object.
                graphicOverlay.add(
                        ObjectGraphicInProminentMode(
                                graphicOverlay, objects[0], confirmationController
                        )
                )
                graphicOverlay.add(ObjectReticleGraphic(graphicOverlay, cameraReticleAnimator))
                cameraReticleAnimator.start()
            }
        }
        graphicOverlay.invalidate()
    }

    private fun objectBoxOverlapsConfirmationReticle(
        graphicOverlay: GraphicOverlay,
        visionObject: DetectedObject
    ): Boolean {
        val boxRect = graphicOverlay.translateRect(visionObject.boundingBox)
        val reticleCenterX = graphicOverlay.width / 2f
        val reticleCenterY = graphicOverlay.height / 2f
        val reticleRect = RectF(
                reticleCenterX - reticleOuterRingRadius,
                reticleCenterY - reticleOuterRingRadius,
                reticleCenterX + reticleOuterRingRadius,
                reticleCenterY + reticleOuterRingRadius
        )
        return reticleRect.intersect(boxRect)
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Object detection failed!", e)
    }

    companion object {
        private const val TAG = "ProminentObjProcessor"
    }
}
