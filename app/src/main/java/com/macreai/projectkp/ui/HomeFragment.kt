package com.macreai.projectkp.ui

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.macreai.projectkp.R
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentHomeBinding
import com.macreai.projectkp.util.*
import com.macreai.projectkp.util.Helper.examplePointChart1
import com.macreai.projectkp.util.Helper.examplePointChart2
import com.macreai.projectkp.util.Helper.exampleTimeChart
import com.macreai.projectkp.util.Helper.millisToSeconds
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isPlaying = false

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    private var startTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        initViewChart1()
        // initViewChart2()

        getDeviceDetail()

        checkLoginUser()

        viewModel.message.observe(this, Observer {  message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        binding.cardViewUser.setOnClickListener {
            if (isPlaying) {
                Toast.makeText(requireContext(), "Please Stop the Device First", Toast.LENGTH_SHORT).show()
            } else {
                // Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_editFragment)
                view.findNavController()?.navigate(R.id.action_homeFragment_to_editFragment)
            }
        }

        binding.cardViewDevice.setOnClickListener {
            if (isPlaying) {
                Toast.makeText(requireContext(), "Please Stop the Device First", Toast.LENGTH_SHORT).show()
            } else {
                // Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_selectDeviceFragment)
                view.findNavController()?.navigate(R.id.action_homeFragment_to_selectDeviceFragment)
            }
        }

        binding.btnPlayStop.setImageResource(R.drawable.ic_play)
        binding.btnPlayStop.setOnClickListener {
            if (isPlaying) {
                if (viewModel.connectedDevice.isInitialized){
                    binding.btnPlayStop.setImageResource(R.drawable.ic_play)
                    Toast.makeText(requireContext(), "Stop", Toast.LENGTH_SHORT).show()
                    viewModel.connectedDevice.observe(this, Observer {
                        stopNotifyDevice(it, "f50e45b0-eda3-4a90-8f5a-83172d9f5365", "beb5483e-36e1-4688-b7f5-ea07361b26a8")
                    })
                }
            } else {
                if (viewModel.connectedDevice.isInitialized){
                    binding.btnPlayStop.setImageResource(R.drawable.ic_stop)
                    Toast.makeText(requireContext(), "Play", Toast.LENGTH_SHORT).show()
                    viewModel.connectedDevice.observe(this, Observer {
                        startNotifyData(it, "f50e45b0-eda3-4a90-8f5a-83172d9f5365", "beb5483e-36e1-4688-b7f5-ea07361b26a8")
                    })
                } else {
                    Toast.makeText(requireContext(), "Please Choose the device first", Toast.LENGTH_SHORT).show()
                }
            }

            isPlaying = !isPlaying
        }

        return view
    }

    private fun checkLoginUser() {
        viewModel.getLogin().observe(requireActivity(), Observer {id ->
            if (id == 0){
                //Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_loginFragment)
                view?.findNavController()?.navigate(R.id.action_homeFragment_to_loginFragment)
            }
        })
    }

    private fun getDeviceDetail() {

        viewModel.connectedDevice.observe(this, Observer { device ->
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

    private fun startNotifyData(device: BleDevice?, uuidService: String, uuidRead: String){
        BleManager.getInstance().notify(device, uuidService, uuidRead, object: BleNotifyCallback(){
            override fun onNotifySuccess() {
                Toast.makeText(requireContext(), "Notified", Toast.LENGTH_SHORT).show()
            }

            override fun onNotifyFailure(exception: BleException?) {
                Log.d(TAG, "onNotifyFailure: $exception", )
            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                val currentTime = System.currentTimeMillis()
                if (startTime == 0L) {
                    startTime = currentTime
                }
                val elapsedTime = currentTime - startTime
                val elapsedTimeFloat = millisToSeconds(elapsedTime)

                if (elapsedTimeFloat >= 10.0) {
                    startTime = currentTime
                    exampleTimeChart.clear()
                    examplePointChart1.clear()
                }
                // Log.d(TAG, "onCharacteristicChanged: $examplePointChart1")
                exampleTimeChart.add(elapsedTimeFloat)
                examplePointChart1.add(toInt32(data).toFloat())

                updateChart()
            }

        })
    }

    private fun stopNotifyDevice(device: BleDevice?,uuidService: String, uuidCharacter: String){
        BleManager.getInstance().stopNotify(device, uuidService, uuidCharacter)
        Log.d(TAG, "stopNotifyDevice: ")
    }

    private fun toInt32(bytes:ByteArray?):Int {
        if (bytes?.size != 4) {
            throw Exception("wrong len")
        }
        bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    private fun updateChart() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((elem1, elem2) in exampleTimeChart.zip(examplePointChart1)) {
            dataValues.add(Entry(elem1, elem2))
        }

        val lineDataset1 = LineDataSet(dataValues, "EKG 1")
        lineDataset1.setDrawCircles(false)

        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset1)

        val data = LineData(dataset)
        binding.lineChart1.data = data
        binding.lineChart1.notifyDataSetChanged()
        binding.lineChart1.invalidate()
    }


    private fun initViewChart1() {
        binding.lineChart1.description.text = "EKG 1"
        binding.lineChart1.setNoDataText("No data available")
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
    override fun onResume() {
        super.onResume()
        viewModel.getDetailUser()
        viewModel.user.observe(this, Observer{ user ->
            binding.tvUsername.text = user?.username
            binding.tvAddress.text = user?.address
            binding.tvPhoneNumber.text = user?.phone
            binding.tvAge.text = user?.age
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

    override fun onDestroyView() {
        super.onDestroyView()
        exampleTimeChart.clear()
        examplePointChart1.clear()
        examplePointChart2.clear()
        _binding = null
    }

    companion object{
        private const val TAG = "HomeFragment"
        private const val WHATSAPP_URL = "https://api.whatsapp.com/send?phone=%s"
    }
}