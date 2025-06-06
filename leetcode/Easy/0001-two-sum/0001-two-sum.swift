class Solution {
    func twoSum(_ nums: [Int], _ target: Int) -> [Int] {
    
        for i in 0...nums.count {
            for j in i+1..<nums.count {
                let sum = nums[i] + nums[j];
                if (sum == target) {
                    return [i, j]
                }
            }
        }

        return []
    }
}