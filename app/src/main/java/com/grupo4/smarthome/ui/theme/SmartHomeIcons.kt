package com.grupo4.smarthome.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SpeakerGroup
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.outlined.WorkOutline

object SmartHomeIcons {
    val Home = Icons.Outlined.Home
    val Rooms = Icons.Outlined.MeetingRoom
    val Routines = Icons.Outlined.Bolt
    val Devices = Icons.Default.Devices
    val Search = Icons.Default.Search
    val Add = Icons.Default.Add
    val Favorite = Icons.Default.Star
    val FavoriteBorder = Icons.Outlined.StarBorder
    val Play = Icons.Default.PlayArrow
    val Info = Icons.Outlined.Info
    val Schedule = Icons.Outlined.Schedule
    val ManualTrigger = Icons.Outlined.TouchApp
    val MorningRoutine = Icons.Outlined.WbSunny
    val NightRoutine = Icons.Outlined.DarkMode
    val ComfortRoutine = Icons.Outlined.Weekend
    val SecurityRoutine = Icons.Outlined.VerifiedUser
    val CustomRoutine = Icons.Outlined.Settings
    val Settings = Icons.Outlined.Settings
    val Logout = Icons.AutoMirrored.Filled.Logout
    val Apartment = Icons.Outlined.Apartment
    val Office = Icons.Outlined.WorkOutline
    val Menu = Icons.Default.Menu
    val Profile = Icons.Outlined.Person
    val Close = Icons.Default.Close
    val ExpandMore = Icons.Default.KeyboardArrowDown
    val Selected = Icons.Default.CheckCircle

    // Routine detail
    val Back = Icons.AutoMirrored.Filled.ArrowBack
    val MoreOptions = Icons.Default.MoreVert
    val ChevronRight = Icons.Default.ChevronRight
    val Rename = Icons.Outlined.DriveFileRenameOutline
    val Delete = Icons.Outlined.Delete
    val Check = Icons.Default.Check
    val DragHandle = Icons.Default.DragIndicator
    val Calendar = Icons.Outlined.CalendarMonth
    val Speaker = Icons.Outlined.SpeakerGroup
    val Lightbulb = Icons.Outlined.Lightbulb
    val Thermostat = Icons.Outlined.Thermostat

    // Fallback for future security-specific UI if the checked variant is not desired.
    val Shield = Icons.Outlined.Shield
}
