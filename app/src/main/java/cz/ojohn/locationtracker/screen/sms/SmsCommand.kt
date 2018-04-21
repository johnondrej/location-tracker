package cz.ojohn.locationtracker.screen.sms

/**
 * Class with description about SMS command
 */
data class SmsCommand(val command: String, val descriptionRes: Int, val requiresPassword: Boolean)
