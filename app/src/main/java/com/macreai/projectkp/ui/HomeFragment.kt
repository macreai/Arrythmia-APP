package com.macreai.projectkp.ui

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.macreai.projectkp.R
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentHomeBinding
import com.macreai.projectkp.util.Helper.examplePointChart1
import com.macreai.projectkp.util.Helper.examplePointChart2
import com.macreai.projectkp.util.Helper.exampleTimeChart
import com.macreai.projectkp.util.Helper.millisToSeconds
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isPlaying = false

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    private var startTime: Long = 0

    private var modelJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        initViewChart1()
        initViewChart2()
        initViewChart3()

        getDeviceDetail()

        detailUser()

        binding.cardViewUser.setOnClickListener {
            if (isPlaying) {
                Toast.makeText(requireContext(), "Please Stop the Device First", Toast.LENGTH_SHORT).show()
            } else {
                // Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_editFragment)
                view.findNavController().navigate(R.id.action_homeFragment_to_editFragment)
            }
        }

        binding.cardViewDevice.setOnClickListener {
            if (isPlaying) {
                Toast.makeText(requireContext(), "Please Stop the Device First", Toast.LENGTH_SHORT).show()
            } else {
                // Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_selectDeviceFragment)
                view.findNavController().navigate(R.id.action_homeFragment_to_selectDeviceFragment)
            }
        }

        binding.btnPlayStop.setImageResource(R.drawable.ic_play)
        binding.btnPlayStop.setOnClickListener {
            if (isPlaying) {
                if (viewModel.connectedDevice.isInitialized){
                    binding.btnPlayStop.setImageResource(R.drawable.ic_play)
                    Toast.makeText(requireContext(), "Stop", Toast.LENGTH_SHORT).show()
                    viewModel.connectedDevice.observe(viewLifecycleOwner, Observer {
                        stopNotifyDevice(it, UUID_SERVICE, UUID_CHARACTERISTIC)
                        stopNotifyDevice1(it, UUID_SERVICE, UUID_CHARACTERISTIC_1)
                    })
                }
            } else {
                if (viewModel.connectedDevice.isInitialized){
                    binding.btnPlayStop.setImageResource(R.drawable.ic_stop)
                    Toast.makeText(requireContext(), "Play", Toast.LENGTH_SHORT).show()
                    viewModel.connectedDevice.observe(viewLifecycleOwner, Observer {
                        startNotifyData(it, UUID_SERVICE, UUID_CHARACTERISTIC)
                        startNotifyData1(it, UUID_SERVICE, UUID_CHARACTERISTIC_1)
                    })
                } else {
                    Toast.makeText(requireContext(), "Please Choose the device first", Toast.LENGTH_SHORT).show()
                }
            }

            isPlaying = !isPlaying
        }

        return view
    }

//    private fun checkLoginUser() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.getLogin().observe(requireActivity(), Observer { token ->
//                Log.d(TAG, "checkLoginUser: $token")
//                if (token.isEmpty()) {
//                    //Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_loginFragment)
//                    view?.findNavController()?.navigate(R.id.action_homeFragment_to_loginFragment)
//                } else {
//                    detailUser()
//                }
//            })
//        }
//    }

    private fun getDeviceDetail() {

        viewModel.connectedDevice.observe(viewLifecycleOwner, Observer { device ->
            val isConnected = BleManager.getInstance().isConnected(device.mac)
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceId.text = device.mac

            if (isConnected){
                binding.tvStatus.text = "Connected"
                binding.imgConnection.setImageResource(R.drawable.ic_check)
            } else {
                binding.tvStatus.text = "Disconnect"
                binding.imgConnection.setImageResource(R.drawable.ic_disconnect)
            }
        })

    }

//    private fun startWriteData(device: BleDevice?, uuidService: String, uuidWrite: String, data: ByteArray){
//        BleManager.getInstance().write(device, uuidService, uuidService, data, object: BleWriteCallback(){
//            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onWriteFailure(exception: BleException?) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }

    private fun startNotifyData(device: BleDevice?, uuidService: String, uuidRead: String){
        BleManager.getInstance().notify(device, uuidService, uuidRead, object: BleNotifyCallback(){
            override fun onNotifySuccess() {
                Toast.makeText(requireContext(), "Notified", Toast.LENGTH_SHORT).show()
            }

            override fun onNotifyFailure(exception: BleException?) {
                Log.d(TAG, "onNotifyFailure: $exception", )
                Toast.makeText(requireContext(), "$exception", Toast.LENGTH_SHORT).show()
            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                CoroutineScope(Dispatchers.Main).launch {
                    val currentTime = System.currentTimeMillis()
                    if (startTime == 0L) {
                        startTime = currentTime
                    }
                    val elapsedTime = currentTime - startTime
                    val elapsedTimeFloat = millisToSeconds(elapsedTime)

                if (elapsedTimeFloat >= 10.0) {
                    startTime = currentTime
                    Log.d(TAG, "array time: $exampleTimeChart")
                    Log.d(TAG, "array ekg1: $examplePointChart1")
                    Log.d(TAG, "array ekg2: $examplePointChart2")
                    predict(examplePointChart1.toString(), examplePointChart2.toString())
                    exampleTimeChart.clear()
                    examplePointChart1.clear()
                }

                    Log.d(TAG, "onCharacteristicChanged: ${ByteBuffer.wrap(data!!).order(ByteOrder.LITTLE_ENDIAN).float}")
                    Log.d(TAG, "data: $data!!")
                    exampleTimeChart.add(elapsedTimeFloat)
                    examplePointChart1.add(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).float)

                    updateChart1()
                    updateChart3()
                }

            }

        })
    }

    private fun startNotifyData1(device: BleDevice?, uuidService: String, uuidRead: String){
        BleManager.getInstance().notify(device, uuidService, uuidRead, object : BleNotifyCallback(){
            override fun onNotifySuccess() {

            }

            override fun onNotifyFailure(exception: BleException?) {

            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                CoroutineScope(Dispatchers.Main).launch {
                    val currentTime = System.currentTimeMillis()
                    if (startTime == 0L) {
                        startTime = currentTime
                    }
                    val elapsedTime = currentTime - startTime
                    val elapsedTimeFloat = millisToSeconds(elapsedTime)

                    if (elapsedTimeFloat >= 10.0) {
                        startTime = currentTime
                        examplePointChart2.clear()
                    }

                    Log.d(TAG, "onCharacteristicChanged1: ${ByteBuffer.wrap(data!!).order(ByteOrder.LITTLE_ENDIAN).float}")
                    Log.d(TAG, "data1: $data!!")
                    examplePointChart2.add(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).float)

                    updateChart2()
                }
            }

        })
    }

    private fun stopNotifyDevice(device: BleDevice?,uuidService: String, uuidCharacter: String){
        BleManager.getInstance().stopNotify(device, uuidService, uuidCharacter)
        Log.d(TAG, "stopNotifyDevice: ")
        exampleTimeChart.clear()
        examplePointChart1.clear()
    }

    private fun stopNotifyDevice1(device: BleDevice?, uuidService: String, uuidCharacter: String){
        BleManager.getInstance().stopNotify(device, uuidService, uuidCharacter)
        examplePointChart2.clear()
    }

    private fun updateChart1() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart1)) {
            if (elem1 <= 10) {
                dataValues.add(Entry(elem1, elem2))
            }
        }

        val lineDataset1 = LineDataSet(dataValues, "EKG 1")
        lineDataset1.setDrawCircles(false)
        lineDataset1.setDrawValues(false)

        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset1)

        val data = LineData(dataset)
        binding.lineChart1.data = data

        val lastXValue = exampleTimeChart.last()
        if (lastXValue >= 5f) {
            val nextMultipleOf5 = ((lastXValue / 5).toInt() + 1) * 5
            binding.lineChart1.setVisibleXRange(5f, nextMultipleOf5.toFloat())
        } else {
            binding.lineChart1.setVisibleXRange(1f, 5f)
        }

        binding.lineChart1.xAxis.isEnabled = false
        binding.lineChart1.axisLeft.isEnabled = false
        binding.lineChart1.axisRight.isEnabled = false
        binding.lineChart1.invalidate()

    }


    private fun updateChart2() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart2)) {
            if (elem1 <= 10) {
                dataValues.add(Entry(elem1, elem2))
            }
        }

        val lineDataset2 = LineDataSet(dataValues, "EKG 2")
        lineDataset2.setDrawCircles(false)
        lineDataset2.setDrawValues(false)

        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset2)

        val data = LineData(dataset)
        binding.lineChart2.data = data

        val lastXValue = exampleTimeChart.last()
        if (lastXValue >= 5f) {
            val nextMultipleOf5 = ((lastXValue / 5).toInt() + 1) * 5

            binding.lineChart2.setVisibleXRange(5f, nextMultipleOf5.toFloat())
        } else {
            binding.lineChart2.setVisibleXRange(1f, 5f)
        }

        binding.lineChart2.notifyDataSetChanged()
        binding.lineChart2.xAxis.isEnabled = false
        binding.lineChart2.axisLeft.isEnabled = false
        binding.lineChart2.axisRight.isEnabled = false
        binding.lineChart2.invalidate()
    }

    private fun updateChart3() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart1)) {
            if (elem1 <= 10) {
                dataValues.add(Entry(elem1, elem2))
            }
        }

        val lineDataset3 = LineDataSet(dataValues, "EKG 3")
        lineDataset3.setDrawCircles(false)
        lineDataset3.setDrawValues(false)

        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset3)

        val data = LineData(dataset)
        binding.lineChart3.data = data

        val lastXValue = exampleTimeChart.last()
        if (lastXValue >= 5f) {
            val nextMultipleOf5 = ((lastXValue / 5).toInt() + 1) * 5
            binding.lineChart3.setVisibleXRange(5f, nextMultipleOf5.toFloat())
        } else {
            binding.lineChart3.setVisibleXRange(1f, 5f)
        }

        binding.lineChart3.xAxis.isEnabled = false
        binding.lineChart3.axisLeft.isEnabled = false
        binding.lineChart3.axisRight.isEnabled = false
        binding.lineChart3.invalidate()

    }



    private fun initViewChart1() {
        binding.lineChart1.description.text = "EKG 1"
        binding.lineChart1.setNoDataText("No data available")
    }

    private fun initViewChart2() {
        binding.lineChart2.description.text = "EKG 2"
        binding.lineChart2.setNoDataText("No data available")
    }

    private fun initViewChart3(){
        binding.lineChart3.description.text = "EKG 3"
        binding.lineChart3.setNoDataText("No data available")
    }



    /*
    private fun plottingChart1(): ArrayList<Entry> {
        val dataValues: ArrayList<Entry> = ArrayList()

        // Menambahkan semua data yang ada dalam exampleTimeChart dan examplePointChart1
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart1)) {
            dataValues.add(Entry(elem1, elem2))
        }

        // Menambahkan data terbaru yang belum ditambahkan ke grafik
        for (i in lastPlottedIndex until exampleTimeChart.size) {
            dataValues.add(Entry(exampleTimeChart[i], examplePointChart1[i]))
            lastPlottedIndex = i + 1
        }

        return dataValues
    }

     */
    /*
    private fun initViewChart2() {
        val lineDataset2 = LineDataSet(plottingChart2(), "EKG 2")
        lineDataset2.setDrawCircles(false)
        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset2)
        val data = LineData(dataset)
        binding.lineChart2.data = data
        binding.lineChart2.invalidate()
    }

    private fun plottingChart2(): ArrayList<Entry> {
        val dataValues: ArrayList<Entry> = ArrayList<Entry>()
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart2)){
            val elem1ToFloatOrInt = extractSeconds(elem1)
            dataValues.add(Entry(elem1ToFloatOrInt!!.toFloat(), elem2.toString().toFloat()))
        }
        return dataValues
    }
     */

    /*
    private fun byteArrayToFloat(byteArray: ByteArray?): Float {
        if (byteArray == null || byteArray.size < 4) {
            throw IllegalArgumentException("Invalid byte array")
        }
        val buffer = ByteBuffer.wrap(byteArray)
        return buffer.float
    }
    */

    private fun predict(ekg1: String, ekg2: String){
        lifecycleScope.launchWhenResumed {
            if (modelJob.isActive) modelJob.cancel()
            modelJob = launch {
                viewModel.modelPrediction(ekg1, ekg2).collect{result ->
                    result.onSuccess {
                        Log.d(TAG, "predict: ${it.hasil}")
                        binding.tvOutputModel.text = if (it.hasil == "['N']"){
                            "Normal"
                        } else {
                            "AF"
                        }
                    }
                    result.onFailure {  throwable ->
                        Toast.makeText(requireContext(), "Error Predicting", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "predict: $throwable", )
                        throwable.printStackTrace()
                    }
                }
            }
        }

    }

    private fun detailUser() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "detailUser Thread: ${Thread.currentThread().name}")
            viewModel.getDetailUser()
            viewModel.user.observe(viewLifecycleOwner, Observer { user ->
                binding.tvUsernamePerson.text = user?.username
                binding.tvAddressPerson.text = user?.address
                binding.tvPhoneNumberPerson.text = user?.phone
                binding.tvAgePerson.text = user?.age
                if (user?.gender == "Male"){
                    binding.tvGender.setImageResource(R.drawable.ic_male)
                } else {
                    binding.tvGender.setImageResource(R.drawable.ic_female)
                }
                binding.dialEmergencyPhone.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(String.format(WHATSAPP_URL, user?.emergencyPhone))
                    startActivity(intent)
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exampleTimeChart.clear()
        examplePointChart1.clear()
        examplePointChart2.clear()
        _binding = null
    }

    companion object{
        private const val TAG = "HomeFragment"
        private const val WHATSAPP_URL = "https://api.whatsapp.com/send?phone=+62%s"
        private const val UUID_SERVICE = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        private const val UUID_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
        private const val UUID_CHARACTERISTIC_1 = "beb5483e-36e1-4688-b7f5-ea07361b26a9"
    }
}