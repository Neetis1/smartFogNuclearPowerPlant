package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/* 
 * simulation setup for smart Nuclear power plant
 * Author : Neeti Sharma
 * Module : Fog and Edge Computing
 * College : National College of Ireland 
 */

public class smartNuclearPowerPlant {
	// to create fog devices
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	// to create sensors
	static List<Sensor> sensors = new ArrayList<Sensor>();
	// to create actuators
	static List<Actuator> actuators = new ArrayList<Actuator>();

	// Define the number of fog nodes will be deployed
	static int numOfFogDevices = 2;
	// Define the number of temperature sensors with each fog nodes
	static int numOfTempSensorsPerArea = 15;
	// Define the number of Radiation sensors with each fog nodes
	static int numOfRadSensorsPerArea = 5;
	// Define the number of Motion sensors with each fog nodes
	static int numOfMotionSensorsPerArea = 7;
	// Define the number of Gas Leak sensors with each fog nodes
	static int numOfGasSensorsPerArea = 10;
	// Define the number of Air Quality sensors with each fog nodes
	static int numOfAirSensorsPerArea = 5;
	// We are using the fog nodes to perform the operations.
	// cloud is set to false
	private static boolean CLOUD = true;

	public static void main(String[] args) {
		Log.printLine("Smart Nuclear Power Plant Management system...");
		try {
			Log.disable();

			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events
			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "mins"; // identifier of the application
			FogBroker broker = new FogBroker("broker");

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			createFogDevices(broker.getId(), appId);

			// initialising a module mapping
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

//			for (FogDevice device : fogDevices) {
//				if (device.getName().startsWith("z")) {
//					moduleMapping.addModuleToDevice("master-module", device.getName());
//				}
//				if (device.getName().startsWith("g")) {
//					moduleMapping.addModuleToDevice("gasinfo-module", device.getName());
//				}
//
//				if (device.getName().startsWith("t")) {
//					moduleMapping.addModuleToDevice("tempertaureinfo-module", device.getName());
//				}
//				if (device.getName().startsWith("m")) {
//					moduleMapping.addModuleToDevice("motioninfo-module", device.getName());
//				}
//				if (device.getName().startsWith("r")) {
//					moduleMapping.addModuleToDevice("radiationinfo-module", device.getName());
//				}
//				if (device.getName().startsWith("a")) {
//					moduleMapping.addModuleToDevice("airinfo-module", device.getName());
//				}
//
//			}
			// if the mode of deployment is cloud-based
			if (CLOUD) {
				// placing all instances of master-module in Cloud
				moduleMapping.addModuleToDevice("master-module", "cloud");
				moduleMapping.addModuleToDevice("gasinfo-module", "cloud");
				moduleMapping.addModuleToDevice("tempertaureinfo-module", "cloud");
				moduleMapping.addModuleToDevice("motioninfo-module", "cloud");
				moduleMapping.addModuleToDevice("radiationinfo-module", "cloud");
				moduleMapping.addModuleToDevice("airinfo-module", "cloud");
			} else {
				for (FogDevice device : fogDevices) {
					if (device.getName().startsWith("z")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
					} else if (device.getName().startsWith("g")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
						moduleMapping.addModuleToDevice("gasinfo-module", device.getName());
					} else if (device.getName().startsWith("t")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
						moduleMapping.addModuleToDevice("tempertaureinfo-module", device.getName());
					} else if (device.getName().startsWith("m")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
						moduleMapping.addModuleToDevice("motioninfo-module", device.getName());
					} else if (device.getName().startsWith("r")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
						moduleMapping.addModuleToDevice("radiationinfo-module", device.getName());
					} else if (device.getName().startsWith("a")) {
						moduleMapping.addModuleToDevice("master-module", device.getName());
						moduleMapping.addModuleToDevice("airinfo-module", device.getName());
					}
				}
//					moduleMapping.addModuleToDevice("gasinfo-module", device.getName());
//					moduleMapping.addModuleToDevice("tempertaureinfo-module", device.getName());
//					moduleMapping.addModuleToDevice("motioninfo-module", device.getName());
//					moduleMapping.addModuleToDevice("radiationinfo-module", device.getName());
//					moduleMapping.addModuleToDevice("airinfo-module", device.getName());

			}

			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);

			controller.submitApplication(application, 0, (CLOUD)
					? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
					: (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();
			Log.printLine(" Nuclear Power plant simulation finished!");
		} catch (

		Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createFogDevices(int userId, String appId) {
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 10000, 10000, 0, 0.01, 16 * 103, 16 * 83.25); // creates
																												// the
																												// fog
																												// device
																												// Cloud
																												// at
																												// the
																												// apex
																												// of
																												// the
																												// hierarchy
																												// with
																												// level=0
		cloud.setParentId(-1);
		FogDevice proxy = createFogDevice("proxy-server", 4800, 8000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates
																												// the
																												// fog
																												// device
																												// Proxy
																												// Server
																												// (level=1)
		proxy.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
		proxy.setUplinkLatency(100); // latency of connection from Proxy Server to the Cloud is 100 ms

		fogDevices.add(cloud);
		fogDevices.add(proxy);

		for (int i = 0; i < numOfFogDevices; i++) {
			addFogNode(i + "", userId, appId, proxy.getId());
		}
	}

	private static FogDevice addFogNode(String id, int userId, String appId, int parentId) {
		FogDevice fognode = createFogDevice("z-" + id, 2800, 4000, 1000, 1000, 1, 0.0, 107.339, 83.4333);
		fogDevices.add(fognode);
		fognode.setParentId(parentId);
		fognode.setUplinkLatency(4);
		for (int i = 0; i < numOfGasSensorsPerArea; i++) {
			FogDevice gas = addGasSensors(i + "", userId, appId, fognode.getId());
			fogDevices.add(gas);
		}
		for (int i = 0; i < numOfTempSensorsPerArea; i++) {
			FogDevice temp = addTempSensors(i + "", userId, appId, fognode.getId());
			fogDevices.add(temp);
		}
		for (int i = 0; i < numOfRadSensorsPerArea; i++) {
			FogDevice rad = addRadSensors(i + "", userId, appId, fognode.getId());
			fogDevices.add(rad);
		}
		for (int i = 0; i < numOfMotionSensorsPerArea; i++) {
			FogDevice mot = addMotSensors(i + "", userId, appId, fognode.getId());
			fogDevices.add(mot);
		}
		for (int i = 0; i < numOfAirSensorsPerArea; i++) {
			FogDevice air = addAirSensors(i + "", userId, appId, fognode.getId());
			fogDevices.add(air);
		}

		return fognode;
	}

	private static FogDevice addGasSensors(String id, int userId, String appId, int parentId) {
		FogDevice gasSensor = createFogDevice("g-" + id, 1000, 1000, 100, 100, 3, 0, 87.53, 82.44);
		gasSensor.setParentId(parentId);
		Sensor sensor = new Sensor("sg-" + id, "GAS", userId, appId, new DeterministicDistribution(5));
		sensors.add(sensor);
		Actuator display = new Actuator("actg-" + id, userId, appId, "ACT_CONTROLGAS");

		actuators.add(display);
//		sensor.setGatewayDeviceId(gasSensor.getId());
		sensor.setGatewayDeviceId(gasSensor.getId());
		sensor.setLatency(2.0);
		display.setGatewayDeviceId(gasSensor.getId());
		display.setLatency(2.0);

		return gasSensor;
	}

	private static FogDevice addTempSensors(String id, int userId, String appId, int parentId) {
		FogDevice tempSensor = createFogDevice("t-" + id, 1000, 1000, 100, 100, 3, 0, 87.53, 82.44);
		tempSensor.setParentId(parentId);
		Sensor sensor = new Sensor("st-" + id, "TMP", userId, appId, new DeterministicDistribution(5));
		sensors.add(sensor);
		Actuator display1 = new Actuator("actt-" + id, userId, appId, "ACT_CONTROLTMP");

		actuators.add(display1);
		sensor.setGatewayDeviceId(tempSensor.getId());
		sensor.setLatency(2.0);
		display1.setGatewayDeviceId(tempSensor.getId());
		display1.setLatency(2.0);
		return tempSensor;
	}

	private static FogDevice addRadSensors(String id, int userId, String appId, int parentId) {
		FogDevice radSensor = createFogDevice("r-" + id, 1000, 1000, 100, 100, 3, 0, 87.53, 82.44);
		radSensor.setParentId(parentId);
		Sensor sensor = new Sensor("sr-" + id, "RAD", userId, appId, new DeterministicDistribution(5));
		sensors.add(sensor);
		Actuator display2 = new Actuator("actr-" + id, userId, appId, "ACT_CONTROLRAD");

		actuators.add(display2);
		sensor.setGatewayDeviceId(radSensor.getId());
		sensor.setLatency(2.0);
		display2.setGatewayDeviceId(radSensor.getId());
		display2.setLatency(2.0);
		return radSensor;
	}

	private static FogDevice addMotSensors(String id, int userId, String appId, int parentId) {
		FogDevice motSensor = createFogDevice("m-" + id, 1000, 1000, 100, 100, 3, 0, 87.53, 82.44);
		motSensor.setParentId(parentId);
		Sensor sensor = new Sensor("sm-" + id, "MOT", userId, appId, new DeterministicDistribution(5));
		sensors.add(sensor);
		Actuator display3 = new Actuator("actm-" + id, userId, appId, "ACT_CONTROLMOT");

		actuators.add(display3);
		sensor.setGatewayDeviceId(motSensor.getId());
		sensor.setLatency(2.0);
		display3.setGatewayDeviceId(motSensor.getId());
		display3.setLatency(2.0);
		return motSensor;
	}

	private static FogDevice addAirSensors(String id, int userId, String appId, int parentId) {
		FogDevice airSensor = createFogDevice("a-" + id, 1000, 1000, 100, 100, 3, 0, 87.53, 82.44);
		airSensor.setParentId(parentId);
		Sensor sensor = new Sensor("sa-" + id, "AIR", userId, appId, new DeterministicDistribution(5));
		sensors.add(sensor);
		Actuator display4 = new Actuator("acta-" + id, userId, appId, "ACT_CONTROLAIR");

		actuators.add(display4);
		sensor.setGatewayDeviceId(airSensor.getId());
		sensor.setLatency(2.0);
		display4.setGatewayDeviceId(airSensor.getId());
		display4.setLatency(2.0);
		return airSensor;
	}

	/**
	 * Creates a vanilla fog device
	 * 
	 * @param nodeName    name of the device to be used in simulation
	 * @param mips        MIPS
	 * @param ram         RAM
	 * @param upBw        uplink bandwidth
	 * @param downBw      downlink bandwidth
	 * @param level       hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower
	 * @param idlePower
	 * @return
	 */
	private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw, int level,
			double ratePerMips, double busyPower, double idlePower) {

		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(hostId, new RamProvisionerSimple(ram), new BwProvisionerOverbooking(bw), storage,
				peList, new StreamOperatorScheduler(peList), new FogLinearPowerModel(busyPower, idlePower));

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
		// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList), storageList,
					10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogdevice.setLevel(level);
		return fogdevice;
	}
	// In function snippet we will create modules and map it to the fog devices.

	private static Application createApplication(String appId, int userId) {

		Application application = Application.createApplication(appId, userId);
		application.addAppModule("master-module", 10);
		application.addAppModule("gasinfo-module", 10);
		application.addAppModule("tempertaureinfo-module", 10);
		application.addAppModule("motioninfo-module", 10);
		application.addAppModule("radiationinfo-module", 10);
		application.addAppModule("airinfo-module", 10);

		// edge between sensors and master modules
		application.addAppEdge("GAS", "master-module", 500, 20000, "GAS", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("TMP", "master-module", 500, 20000, "TMP", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("RAD", "master-module", 500, 20000, "RAD", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("MOT", "master-module", 500, 20000, "MOT", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("AIR", "master-module", 500, 20000, "AIR", Tuple.UP, AppEdge.SENSOR);

		// edge between master module and individual modules
		application.addAppEdge("master-module", "gasinfo-module", 1000, 2000, "gasTask", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("master-module", "tempertaureinfo-module", 1000, 2000, "tmpTask", Tuple.UP,
				AppEdge.MODULE);
		application.addAppEdge("master-module", "motioninfo-module", 1000, 2000, "motTask", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("master-module", "radiationinfo-module", 1000, 2000, "radTask", Tuple.UP,
				AppEdge.MODULE);
		application.addAppEdge("master-module", "airinfo-module", 1000, 2000, "airTask", Tuple.UP, AppEdge.MODULE);

		// edge between Response and master module
		application.addAppEdge("gasinfo-module", "master-module", 2000, 10000, "gasResponse", Tuple.DOWN,
				AppEdge.MODULE);
		application.addAppEdge("tempertaureinfo-module", "master-module", 2000, 10000, "tmpResponse", Tuple.DOWN,
				AppEdge.MODULE);
		application.addAppEdge("motioninfo-module", "master-module", 2000, 10000, "motResponse", Tuple.DOWN,
				AppEdge.MODULE);
		application.addAppEdge("radiationinfo-module", "master-module", 2000, 10000, "radResponse", Tuple.DOWN,
				AppEdge.MODULE);
		application.addAppEdge("airinfo-module", "master-module", 2000, 10000, "airResponse", Tuple.DOWN,
				AppEdge.MODULE);

		// edge between master module and actuators
		application.addAppEdge("master-module", "ACT_CONTROLGAS", 400, 700, "OutputGas", Tuple.DOWN, AppEdge.ACTUATOR);
		application.addAppEdge("master-module", "ACT_CONTROLTMP", 400, 700, "OutputTmp", Tuple.DOWN, AppEdge.ACTUATOR);
		application.addAppEdge("master-module", "ACT_CONTROLMOT", 400, 700, "OutputMot", Tuple.DOWN, AppEdge.ACTUATOR);
		application.addAppEdge("master-module", "ACT_CONTROLRAD", 400, 700, "OutputRad", Tuple.DOWN, AppEdge.ACTUATOR);
		application.addAppEdge("master-module", "ACT_CONTROLAIR", 400, 700, "OutputAir", Tuple.DOWN, AppEdge.ACTUATOR);

		// tuple mapping between master module and tasks
		application.addTupleMapping("master-module", "GAS", "gasTask", new FractionalSelectivity(1.0));
		application.addTupleMapping("master-module", "TMP", "tmpTask", new FractionalSelectivity(1.0));
		application.addTupleMapping("master-module", "MOT", "motTask", new FractionalSelectivity(1.0));
		application.addTupleMapping("master-module", "RAD", "radTask", new FractionalSelectivity(1.0));
		application.addTupleMapping("master-module", "AIR", "airTask", new FractionalSelectivity(1.0));

		// tuple mapping between individual modules and responses
		application.addTupleMapping("gasinfo-module", "gasTask", "gasResponse", new FractionalSelectivity(1.0));
		application.addTupleMapping("tempertaureinfo-module", "tmpTask", "tmpResponse", new FractionalSelectivity(1.0));
		application.addTupleMapping("motioninfo-module", "motTask", "motResponse", new FractionalSelectivity(1.0));
		application.addTupleMapping("radiationinfo-module", "radTask", "radResponse", new FractionalSelectivity(1.0));
		application.addTupleMapping("airinfo-module", "airTask", "airResponse", new FractionalSelectivity(1.0));

		application.addTupleMapping("master-module", "gasResponse", "OutputGas", new FractionalSelectivity(0.1));
		application.addTupleMapping("master-module", "tmpResponse", "OutputTmp", new FractionalSelectivity(0.1));
		application.addTupleMapping("master-module", "motResponse", "OutputMot", new FractionalSelectivity(0.1));
		application.addTupleMapping("master-module", "radResponse", "OutputRad", new FractionalSelectivity(0.1));
		application.addTupleMapping("master-module", "airResponse", "OutputAir", new FractionalSelectivity(0.1));

		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
			{
				add("GAS");
				add("master-module");
				add("gasinfo-module");
				add("master-module");
				add("ACT_CONTROLGAS");
			}
		});
		// final AppLoop loop1 = new AppLoop(new
		// ArrayList<String>(){{add("Sensor");add("MasterModule");add("WorkerModule-1");add("MasterModule");add("Actuator");}});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>() {
			{
				add("TMP");
				add("master-module");
				add("tempertaureinfo-module");
				add("master-module");
				add("ACT_CONTROLTMP");
			}
		});

		final AppLoop loop3 = new AppLoop(new ArrayList<String>() {
			{
				add("MOT");
				add("master-module");
				add("motioninfo-module");
				add("master-module");
				add("ACT_CONTROLMOT");
			}
		});
		final AppLoop loop4 = new AppLoop(new ArrayList<String>() {
			{
				add("RAD");
				add("master-module");
				add("radiationinfo-module");
				add("master-module");
				add("ACT_CONTROLRAD");
			}
		});
		final AppLoop loop5 = new AppLoop(new ArrayList<String>() {
			{
				add("AIR");
				add("master-module");
				add("airinfo-module");
				add("master-module");
				add("ACT_CONTROLAIR");
			}
		});

		List<AppLoop> loops = new ArrayList<AppLoop>() {
			{
				add(loop1);
				add(loop2);
				add(loop3);
				add(loop4);
				add(loop5);
			}
		};

		application.setLoops(loops);
		return application;
	}
}