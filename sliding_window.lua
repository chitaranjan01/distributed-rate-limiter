-- Sliding Window Log Rate Limiter
-- KEYS[1] = the Redis key (e.g., "ratelimit:log:192.168.1.5")
-- ARGV[1] = current timestamp in milliseconds
-- ARGV[2] = window size in milliseconds (e.g., 60000 for 60 seconds)
-- ARGV[3] = request limit (e.g., 5)
-- ARGV[4] = unique member ID (timestamp:uuid)

-- Step 1: Calculate the window start time
local windowStart = tonumber(ARGV[1]) - tonumber(ARGV[2])

-- Step 2: Remove all entries older than windowStart
redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', windowStart)

-- Step 3: Count how many requests are in the current window
local currentCount = redis.call('ZCARD', KEYS[1])

-- Step 4: Check if we're under the limit
local limit = tonumber(ARGV[3])
local allowed = 0
local remaining = 0

if currentCount < limit then
    -- ALLOWED: Add the new request
    redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4])
    allowed = 1
    remaining = limit - currentCount - 1
    
    -- Set TTL to windowSize * 2 (so key auto-deletes if client goes quiet)
    redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[2]) * 2)
else
    -- BLOCKED: Don't add the request
    remaining = 0
end

-- Step 5: Return the result
-- Format: {allowed, limit, remaining, windowStart, windowEnd}
local windowEnd = windowStart + tonumber(ARGV[2])
return {allowed, limit, remaining, windowStart, windowEnd}
