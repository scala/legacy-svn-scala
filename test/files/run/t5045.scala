import util.matching._

object Test {
    def main(args: Array[String]) {
        SI_5045.run()
    }
}

object SI_5045 {
    final val targetDate = "2011-09-29"
    final val target = "The copyright date: " + targetDate
    final val pattern = """(\d{4}-\d\d-\d\d)"""

    def run() {
        expectNone(pattern.r, true, "default")
        expectNone(pattern.r.anchor, true, "anchor")
        expectSome(pattern.r.unanchor, false, "unanchor")
        expectNone(pattern.r.unanchor.anchor, true, "unanchor.anchor")
    }

    def runTest(expected: Option[String])(re: Regex, exp_anchored: Boolean, msg: String) {
        val got = target match {
            case re(when) => Some(when)
            case _ => None
        }

        assert(got == expected, "not ok - " + msg)
        assert(re.isAnchored == exp_anchored, "not ok - " + msg + "(isAnchored)")
    }

    val expectSome = runTest(Some(targetDate)) _
    val expectNone = runTest(None) _
}
