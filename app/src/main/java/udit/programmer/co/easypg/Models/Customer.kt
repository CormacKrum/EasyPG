package udit.programmer.co.easypg.Models

class Customer() {

    var customerId: String = ""
    var name: String = ""
    var image: String = ""
    var gender: String = ""
    var fatherName: String = ""
    var motherName: String = ""
    var fatherMobileNo: String = ""
    var dob: String = ""
    var mobileNumber: String = ""
    var state: String = ""
    var district: String = ""
    var pinCode: String = ""
    var address: String = ""
    var email: String = ""
    var username: String = ""
    var password: String = ""
    var college: String = ""
    var favourites: MutableList<String>? = null

    constructor(
        customerId: String,
        name: String,
        image: String,
        gender: String,
        fatherName: String,
        motherName: String,
        fatherMobileNo: String,
        dob: String,
        mobileNumber: String,
        state: String,
        district: String,
        pinCode: String,
        address: String,
        email: String,
        username: String,
        password: String,
        college: String
    ) : this() {
        this.customerId = customerId
        this.name = name
        this.image = image
        this.gender = gender
        this.fatherName = fatherName
        this.motherName = motherName
        this.fatherMobileNo = fatherMobileNo
        this.dob = dob
        this.mobileNumber = mobileNumber
        this.state = state
        this.district = district
        this.pinCode = pinCode
        this.address = address
        this.email = email
        this.username = username
        this.password = password
        this.college = college
    }
}