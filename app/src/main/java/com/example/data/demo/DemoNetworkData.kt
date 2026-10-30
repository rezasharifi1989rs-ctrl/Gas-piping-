package com.example.data.demo

import com.example.data.models.AddressAnnotation
import com.example.data.models.Crossing
import com.example.data.models.GasNetworkDataset
import com.example.data.models.GasStation
import com.example.data.models.LineEndMarker
import com.example.data.models.Pipe
import com.example.data.models.Valve

/**
 * Built-in demo gas network for offline testing and verification.
 * Labeled explicitly as DEMO («نمونه‌ی نمایشی (DEMO)»).
 *
 * Contains all test cases required by the specification:
 * 1. Straight pipe with two valves (12V1, 12V2)
 * 2. Gas station (CGS-1) with feeder pipe without valve (station warning test)
 * 3. Pipe changing color in the middle (Blue to Green at x=200, y=160)
 * 4. T-junction with three valves (24V1, 24V2, 24V3)
 * 5. Capped dead end with semicircle line-end marker
 * 6. Crossing of two different-color pipes (Blue vs Red) with 4 valves (30V1, 30V2, 31V1, 31V2)
 * 7. Crossing of two same-color pipes (Green vs Green) with 4 valves (40V1, 40V2, 41V1, 41V2)
 * 8. Pipe with a deliberate small gap (flagged during import/self-test)
 * 9. Persian addresses associated with valves
 */
object DemoNetworkData {

    // AutoCAD color indices
    const val COLOR_INDEX_RED = 1     // 0xFFFF5252
    const val COLOR_INDEX_GREEN = 3   // 0xFF00E676
    const val COLOR_INDEX_CYAN = 4    // 0xFF00E5FF
    const val COLOR_INDEX_BLUE = 5    // 0xFF2979FF

    const val COLOR_RGB_RED = 0xFFFF5252
    const val COLOR_RGB_GREEN = 0xFF00E676
    const val COLOR_RGB_CYAN = 0xFF00E5FF
    const val COLOR_RGB_BLUE = 0xFF2979FF

    fun createDataset(): GasNetworkDataset {
        val pipes = listOf(
            // 1. Straight pipe with two valves (Scenario 1)
            Pipe(
                id = "PIPE_STRAIGHT",
                points = doubleArrayOf(50.0, 60.0, 300.0, 60.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),

            // 2. Feeder from Station CGS-1 to first valve (No valve between station and x=100)
            Pipe(
                id = "PIPE_STATION_FEEDER_A",
                points = doubleArrayOf(50.0, 160.0, 100.0, 160.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),

            // From valve 101V1 to color transition point
            Pipe(
                id = "PIPE_FEEDER_B",
                points = doubleArrayOf(100.0, 160.0, 160.0, 160.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),

            // 3. Pipe that changes color in the middle (Scenario 9)
            // First segment (Blue):
            Pipe(
                id = "PIPE_COLOR_CHANGE_BLUE",
                points = doubleArrayOf(160.0, 160.0, 200.0, 160.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),
            // Second segment (Green): continuous at (200.0, 160.0) without junction or crossing
            Pipe(
                id = "PIPE_COLOR_CHANGE_GREEN",
                points = doubleArrayOf(200.0, 160.0, 250.0, 160.0),
                colorIndex = COLOR_INDEX_GREEN,
                colorRgb = COLOR_RGB_GREEN
            ),

            // 4. T-Junction at (250.0, 160.0)
            // North branch leading to dead end
            Pipe(
                id = "PIPE_BRANCH_NORTH",
                points = doubleArrayOf(250.0, 160.0, 250.0, 270.0),
                colorIndex = COLOR_INDEX_GREEN,
                colorRgb = COLOR_RGB_GREEN
            ),

            // East arm leading from T-junction towards crossing
            Pipe(
                id = "PIPE_MAIN_EAST_ARM",
                points = doubleArrayOf(250.0, 160.0, 320.0, 160.0),
                colorIndex = COLOR_INDEX_GREEN,
                colorRgb = COLOR_RGB_GREEN
            ),

            // 5. Crossing of two different-color pipes (Scenario 7)
            // Horizontal pipe (Blue): crosses at (350.0, 160.0)
            Pipe(
                id = "PIPE_CROSS_DIFF_HORIZ",
                points = doubleArrayOf(320.0, 160.0, 440.0, 160.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),
            // Vertical pipe (Red): crosses at (350.0, 160.0)
            Pipe(
                id = "PIPE_CROSS_DIFF_VERT",
                points = doubleArrayOf(350.0, 80.0, 350.0, 240.0),
                colorIndex = COLOR_INDEX_RED,
                colorRgb = COLOR_RGB_RED
            ),

            // 6. Crossing of two same-color pipes (Scenario 8)
            // Horizontal pipe (Green): crosses at (380.0, 310.0)
            Pipe(
                id = "PIPE_CROSS_SAME_HORIZ",
                points = doubleArrayOf(300.0, 310.0, 460.0, 310.0),
                colorIndex = COLOR_INDEX_GREEN,
                colorRgb = COLOR_RGB_GREEN
            ),
            // Vertical pipe (Green): crosses at (380.0, 310.0)
            Pipe(
                id = "PIPE_CROSS_SAME_VERT",
                points = doubleArrayOf(380.0, 230.0, 380.0, 390.0),
                colorIndex = COLOR_INDEX_GREEN,
                colorRgb = COLOR_RGB_GREEN
            ),

            // 7. Deliberate pipe gap for cleaning / import report (Scenario 5)
            Pipe(
                id = "PIPE_GAP_A",
                points = doubleArrayOf(70.0, 310.0, 140.0, 310.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            ),
            Pipe(
                id = "PIPE_GAP_B",
                points = doubleArrayOf(144.0, 310.0, 210.0, 310.0),
                colorIndex = COLOR_INDEX_BLUE,
                colorRgb = COLOR_RGB_BLUE
            )
        )

        val valves = listOf(
            // Straight pipe valves
            Valve(
                id = "V_12V1",
                name = "12V1",
                x = 120.0,
                y = 60.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_STRAIGHT",
                nearestAddress = "خیابان سعدی، پلاک ۱۰",
                distanceToAddress = 15.0
            ),
            Valve(
                id = "V_12V2",
                name = "12V2",
                x = 240.0,
                y = 60.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_STRAIGHT",
                nearestAddress = "خیابان سعدی، نبش کوچه یاس",
                distanceToAddress = 18.0
            ),

            // Station line valve
            Valve(
                id = "V_101V1",
                name = "101V1",
                x = 100.0,
                y = 160.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_STATION_FEEDER_A",
                nearestAddress = "خیابان آزادی، بعد از میدان گاز، پلاک ۴۲",
                distanceToAddress = 22.0
            ),

            // T-junction valves (one on each arm)
            Valve(
                id = "V_24V1",
                name = "24V1",
                x = 230.0,
                y = 160.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_COLOR_CHANGE_GREEN",
                nearestAddress = "بلوار جمهوری اسلامی، کوچه لاله سوم",
                distanceToAddress = 25.0
            ),
            Valve(
                id = "V_24V2",
                name = "24V2",
                x = 250.0,
                y = 195.0,
                rotationDegrees = 90.0,
                pipeId = "PIPE_BRANCH_NORTH",
                nearestAddress = "خیابان فردوسی، نرسیده به بن‌بست شقایق",
                distanceToAddress = 30.0
            ),
            Valve(
                id = "V_24V3",
                name = "24V3",
                x = 275.0,
                y = 160.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_MAIN_EAST_ARM",
                nearestAddress = "چهارراه معلم، ابتدای خیابان بعثت، پلاک ۸",
                distanceToAddress = 28.0
            ),

            // 4 valves around different-color crossing at (350.0, 160.0)
            Valve(
                id = "V_30V1",
                name = "30V1",
                x = 330.0,
                y = 160.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_CROSS_DIFF_HORIZ",
                nearestAddress = "خیابان بعثت، نبش کوچه شهید رضایی",
                distanceToAddress = 20.0
            ),
            Valve(
                id = "V_30V2",
                name = "30V2",
                x = 410.0,
                y = 160.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_CROSS_DIFF_HORIZ",
                nearestAddress = "خیابان بعثت، روبروی مرکز مخابرات",
                distanceToAddress = 19.0
            ),
            Valve(
                id = "V_31V1",
                name = "31V1",
                x = 350.0,
                y = 110.0,
                rotationDegrees = 90.0,
                pipeId = "PIPE_CROSS_DIFF_VERT",
                nearestAddress = "کوچه یاسمن، درب دوم",
                distanceToAddress = 16.0
            ),
            Valve(
                id = "V_31V2",
                name = "31V2",
                x = 350.0,
                y = 210.0,
                rotationDegrees = 90.0,
                pipeId = "PIPE_CROSS_DIFF_VERT",
                nearestAddress = "خیابان ولیعصر، جنب بوستان نرگس",
                distanceToAddress = 24.0
            ),

            // 4 valves around same-color crossing at (380.0, 310.0)
            Valve(
                id = "V_40V1",
                name = "40V1",
                x = 335.0,
                y = 310.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_CROSS_SAME_HORIZ",
                nearestAddress = "شهرک ولیعصر، نبش نگارستان دوم",
                distanceToAddress = 26.0
            ),
            Valve(
                id = "V_40V2",
                name = "40V2",
                x = 425.0,
                y = 310.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_CROSS_SAME_HORIZ",
                nearestAddress = "شهرک ولیعصر، انتهای خیابان نگارستان",
                distanceToAddress = 32.0
            ),
            Valve(
                id = "V_41V1",
                name = "41V1",
                x = 380.0,
                y = 265.0,
                rotationDegrees = 90.0,
                pipeId = "PIPE_CROSS_SAME_VERT",
                nearestAddress = "خیابان شریعتی، کوچه گلها",
                distanceToAddress = 21.0
            ),
            Valve(
                id = "V_41V2",
                name = "41V2",
                x = 380.0,
                y = 355.0,
                rotationDegrees = 90.0,
                pipeId = "PIPE_CROSS_SAME_VERT",
                nearestAddress = "خیابان شریعتی، بن‌بست سرو",
                distanceToAddress = 18.0
            ),

            // Valve on pipe with gap
            Valve(
                id = "V_50V1",
                name = "50V1",
                x = 180.0,
                y = 310.0,
                rotationDegrees = 0.0,
                pipeId = "PIPE_GAP_B",
                nearestAddress = "بلوار فرهنگ، کوچه دانایی ۴",
                distanceToAddress = 14.0
            )
        )

        val stations = listOf(
            GasStation(
                id = "STATION_CGS1",
                name = "CGS-1 (ورودی اصلی شهر)",
                capacity = "400 m³/h",
                tipX = 50.0,
                tipY = 160.0,
                rotationDegrees = 180.0
            )
        )

        val endMarkers = listOf(
            LineEndMarker(
                id = "END_BRANCH_NORTH",
                centerX = 250.0,
                centerY = 270.0,
                radius = 6.0,
                angleDegrees = 90.0
            )
        )

        val addresses = listOf(
            AddressAnnotation(
                id = "ADDR_1",
                text = "بلوار پاسداران، روبروی پارک لاله",
                x = 130.0,
                y = 45.0,
                textHeight = 6.0
            ),
            AddressAnnotation(
                id = "ADDR_2",
                text = "خیابان آزادی، بعد از میدان گاز، پلاک ۴۲",
                x = 70.0,
                y = 180.0,
                textHeight = 6.5
            ),
            AddressAnnotation(
                id = "ADDR_3",
                text = "بلوار جمهوری اسلامی، کوچه لاله سوم",
                x = 210.0,
                y = 180.0,
                textHeight = 6.0
            ),
            AddressAnnotation(
                id = "ADDR_4",
                text = "خیابان فردوسی، نرسیده به بن‌بست شقایق",
                x = 230.0,
                y = 285.0,
                textHeight = 6.0
            ),
            AddressAnnotation(
                id = "ADDR_5",
                text = "چهارراه معلم، ابتدای خیابان بعثت، پلاک ۸",
                x = 310.0,
                y = 140.0,
                textHeight = 6.5
            ),
            AddressAnnotation(
                id = "ADDR_6",
                text = "شهرک ولیعصر، نبش نگارستان دوم",
                x = 330.0,
                y = 335.0,
                textHeight = 6.0
            )
        )

        val crossings = listOf(
            Crossing(
                id = "CROSSING_DIFF_COLORS",
                x = 350.0,
                y = 160.0,
                pipe1Id = "PIPE_CROSS_DIFF_HORIZ",
                pipe1ColorIndex = COLOR_INDEX_BLUE,
                pipe2Id = "PIPE_CROSS_DIFF_VERT",
                pipe2ColorIndex = COLOR_INDEX_RED,
                isConnected = false,
                isAmbiguous = false
            ),
            Crossing(
                id = "CROSSING_SAME_COLORS",
                x = 380.0,
                y = 310.0,
                pipe1Id = "PIPE_CROSS_SAME_HORIZ",
                pipe1ColorIndex = COLOR_INDEX_GREEN,
                pipe2Id = "PIPE_CROSS_SAME_VERT",
                pipe2ColorIndex = COLOR_INDEX_GREEN,
                isConnected = true,
                isAmbiguous = true
            )
        )

        return GasNetworkDataset(
            id = "DATASET_DEMO_DEFAULT",
            name = "نمونه‌ی نمایشی (DEMO)",
            isDemo = true,
            importDateShamsi = "۱۴۰۵/۰۸/۰۷",
            pipes = pipes,
            valves = valves,
            stations = stations,
            endMarkers = endMarkers,
            addresses = addresses,
            crossings = crossings,
            scaleMetersPerUnit = 1.0
        )
    }
}
