db.submission.aggregate([
    {
        $match: {
            problemId: 2
        }
    },
    {
        $group: {
            _id: "$evaluation",
            total: {
                $sum: 1
            }
        }
    },
    {
        $sort: {
            total: -1
        }
    }
])

db.submission.aggregate([
    {
        $group: {
            _id: "$problemId",
            firstSubmissionDate: {
                $first: "$submissionDate"
            }
        }
    },
    {
        $sort: {
            _id: -1
        }
    }
])

db.submission.aggregate([
    {
        $match: {
            evaluation: "CORRECT"
        }
    },
    {
        $group: {
            _id: {user: "$userId", problemId: "$problemId", id: "$id"},
            firstSubmissionDate: {
                $first: "$submissionDate"
            }
        }
    },
    {
        $group: {
            _id: "$_id.user",
            problems: { $addToSet: {id: "$_id.id", problemId: "$_id.problemId", submissionDate: "$firstSubmissionDate"}}
        }
    },
    {
        $sort: {
            _id: 1
        }
    }
]).pretty()

db.submission.aggregate([
    {
        $match: {
            evaluation: "CORRECT"
        }
    },
    {
        $lookup: {
            from: "problem",
            localField: "problemId",
            foreignField: "id",
            as: "problem"
        }
    }
]).pretty()