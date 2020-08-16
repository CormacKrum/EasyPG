package udit.programmer.co.easypg.Models

class PG() {
    var pgId = ""
    var pgName = ""
    var pgType = ""
    var messAvailable = ""
    var liftAvailable = ""
    var pgState = ""
    var pgDistrict = ""
    var pgPincode = ""
    var pgAddress = ""
    var roomType = listOf<String>()
    var personsPerRoom = listOf<String>()
    var numberOfRooms = listOf<String>()
    var numberOfACRooms = listOf<String>()
    var numberOfNonACRooms = listOf<String>()
    var availableNumberOfACRooms = listOf<String>()
    var avaliableNumberOfNonACRooms = listOf<String>()
    var rentOfACRoomsWithoutMess = listOf<String>()
    var rentOfNonACRoomsWithoutMess = listOf<String>()
    var rentOfACRoomsWithMess = listOf<String>()
    var rentOfNonACRoomsWithMess = listOf<String>()
    var latitude = ""
    var longitude = ""
    var images = listOf<String>()
    var ownerName = ""
    var ownerGender = ""
    var ownerDP = ""
    var ownerDOB = ""
    var ownerNumber = ""
    var ownerState = ""
    var ownerDistrict = ""
    var ownerPincode = ""
    var ownerAddress = ""
    var ownerEmail = ""
    var ownerPassWord = ""
    var nearestCollege = ""

    constructor(
        pgId: String,
        pgName: String,
        pgType: String,
        messAvailable: String,
        liftAvailable: String,
        pgState: String,
        pgDistrict: String,
        pgPincode: String,
        pgAddress: String,
        roomType: List<String>,
        personsPerRoom: List<String>,
        numberOfRooms: List<String>,
        numberOfACRooms: List<String>,
        numberOfNonACRooms: List<String>,
        availableNumberOfACRooms: List<String>,
        avaliableNumberOfNonACRooms: List<String>,
        rentOfACRoomsWithoutMess: List<String>,
        rentOfNonACRoomsWithoutMess: List<String>,
        rentOfACRoomsWithMess: List<String>,
        rentOfNonACRoomsWithMess: List<String>,
        latitude: String,
        longitude: String,
        images: List<String>,
        ownerName: String,
        ownerGender: String,
        ownerDP: String,
        ownerDOB: String,
        ownerNumber: String,
        ownerState: String,
        ownerDistrict: String,
        ownerPincode: String,
        ownerAddress: String,
        ownerEmail: String,
        ownerPassWord: String,
        nearestCollege: String
    ) : this() {
        this.pgId = pgId
        this.pgName = pgName
        this.pgType = pgType
        this.messAvailable = messAvailable
        this.liftAvailable = liftAvailable
        this.pgState = pgState
        this.pgDistrict = pgDistrict
        this.pgPincode = pgPincode
        this.pgAddress = pgAddress
        this.roomType = roomType
        this.personsPerRoom = personsPerRoom
        this.numberOfRooms = numberOfRooms
        this.numberOfACRooms = numberOfACRooms
        this.numberOfNonACRooms = numberOfNonACRooms
        this.availableNumberOfACRooms = availableNumberOfACRooms
        this.avaliableNumberOfNonACRooms = avaliableNumberOfNonACRooms
        this.rentOfACRoomsWithoutMess = rentOfACRoomsWithoutMess
        this.rentOfNonACRoomsWithoutMess = rentOfNonACRoomsWithoutMess
        this.rentOfACRoomsWithMess = rentOfACRoomsWithMess
        this.rentOfNonACRoomsWithMess = rentOfNonACRoomsWithMess
        this.latitude = latitude
        this.longitude = longitude
        this.images = images
        this.ownerName = ownerName
        this.ownerGender = ownerGender
        this.ownerDP = ownerDP
        this.ownerDOB = ownerDOB
        this.ownerNumber = ownerNumber
        this.ownerState = ownerState
        this.ownerDistrict = ownerDistrict
        this.ownerPincode = ownerPincode
        this.ownerAddress = ownerAddress
        this.ownerEmail = ownerEmail
        this.ownerPassWord = ownerPassWord
        this.nearestCollege = nearestCollege
    }

    constructor(
        pgId: String,
        pgName: String,
        pgType: String,
        messAvailable: String,
        liftAvailable: String,
        pgState: String,
        pgDistrict: String,
        pgPincode: String,
        pgAddress: String,
        roomType: List<String>,
        personsPerRoom: List<String>,
        numberOfRooms: List<String>,
        numberOfACRooms: List<String>,
        numberOfNonACRooms: List<String>,
        availableNumberOfACRooms: List<String>,
        avaliableNumberOfNonACRooms: List<String>,
        rentOfACRoomsWithoutMess: List<String>,
        rentOfNonACRoomsWithoutMess: List<String>,
        rentOfACRoomsWithMess: List<String>,
        rentOfNonACRoomsWithMess: List<String>,
        ownerName: String,
        ownerGender: String,
        ownerDP: String,
        ownerDOB: String,
        ownerNumber: String,
        ownerState: String,
        ownerDistrict: String,
        ownerPincode: String,
        ownerAddress: String,
        ownerEmail: String,
        ownerPassWord: String,
        nearestCollege: String
    ) : this() {
        this.pgId = pgId
        this.pgName = pgName
        this.pgType = pgType
        this.messAvailable = messAvailable
        this.liftAvailable = liftAvailable
        this.pgState = pgState
        this.pgDistrict = pgDistrict
        this.pgPincode = pgPincode
        this.pgAddress = pgAddress
        this.roomType = roomType
        this.personsPerRoom = personsPerRoom
        this.numberOfRooms = numberOfRooms
        this.numberOfACRooms = numberOfACRooms
        this.numberOfNonACRooms = numberOfNonACRooms
        this.availableNumberOfACRooms = availableNumberOfACRooms
        this.avaliableNumberOfNonACRooms = avaliableNumberOfNonACRooms
        this.rentOfACRoomsWithoutMess = rentOfACRoomsWithoutMess
        this.rentOfNonACRoomsWithoutMess = rentOfNonACRoomsWithoutMess
        this.rentOfACRoomsWithMess = rentOfACRoomsWithMess
        this.rentOfNonACRoomsWithMess = rentOfNonACRoomsWithMess
        this.ownerName = ownerName
        this.ownerGender = ownerGender
        this.ownerDP = ownerDP
        this.ownerDOB = ownerDOB
        this.ownerNumber = ownerNumber
        this.ownerState = ownerState
        this.ownerDistrict = ownerDistrict
        this.ownerPincode = ownerPincode
        this.ownerAddress = ownerAddress
        this.ownerEmail = ownerEmail
        this.ownerPassWord = ownerPassWord
        this.nearestCollege = nearestCollege
    }
}