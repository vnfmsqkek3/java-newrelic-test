-- phone_email_mask.lua (final, hardened)

-- 안전 gsub: 어떤 이유로든 실패하면 원문 반환
local function safe_gsub(s, pattern, repl)
  if type(s) ~= "string" or type(pattern) ~= "string" then
    return s, false
  end
  local ok, res, n = pcall(string.gsub, s, pattern, repl)
  if ok and type(res) == "string" then
    return res, (n or 0) > 0
  end
  return s, false
end

-- 이메일 마스킹 (@ 또는 %40)
local function mask_emails(s)
  local before = s
  local domain_map = { ["naver.com"] = "xxxx", ["gmail.com"] = "yyyyy" }

  -- 로컬파트만 치환, @ 또는 %40 모두 지원
  s, _ = safe_gsub(
    s,
    "([A-Za-z0-9._%%+%-]+)(@|%%40)([A-Za-z0-9.%-]+%.[A-Za-z][A-Za-z]+)",
    function(localpart, sep, domain)
      local d = string.lower(domain)
      local masked = domain_map[d] or "xxxxx"
      if sep == "@" then
        return masked .. "@" .. domain
      else
        -- URL 인코딩된 @ (%40) 유지
        return masked .. "%%40" .. domain
      end
    end
  )
  return s, s ~= before
end

-- 전화번호 마스킹
local function mask_phones(s)
  local before = s

  -- +82 10-1234-5678 / +82-10 1234 5678 / +82 10 1234 5678
  s, _ = safe_gsub(s, "%+82[%s%-]?(10)[%s%-]?(%d%d%d%d)[%s%-]?(%d%d%d%d)", "+82-10-XXXX-XXXX")

  -- 010-1234-5678 / 010 1234 5678 / 01012345678
  s, _ = safe_gsub(s, "(010)[%s%-]?(%d%d%d%d)[%s%-]?(%d%d%d%d)", "%1-XXXX-XXXX")
  s, _ = safe_gsub(s, "(%f[%d])010(%d%d%d%d)(%d%d%d%d)(%f[%D])", "010-XXXX-XXXX")

  -- 02-312-3456 / 02-1234-5678 (공백/하이픈 혼용)
  s, _ = safe_gsub(s, "(02)[%s%-]?(%d%d%d)[%s%-]?(%d%d%d%d)", "%1-XXX-XXXX")
  s, _ = safe_gsub(s, "(02)[%s%-]?(%d%d%d%d)[%s%-]?(%d%d%d%d)", "%1-XXXX-XXXX")
  s, _ = safe_gsub(s, "(%f[%d])02(%d%d%d)(%d%d%d%d)(%f[%D])", "02-XXX-XXXX")

  -- 기타 지역번호: 0xx-123-4567 / 0xx-1234-5678 (공백/하이픈 혼용)
  s, _ = safe_gsub(s, "(0%d%d)[%s%-]?(%d%d%d)[%s%-]?(%d%d%d%d)", "%1-XXX-XXXX")
  s, _ = safe_gsub(s, "(0%d%d)[%s%-]?(%d%d%d%d)[%s%-]?(%d%d%d%d)", "%1-XXXX-XXXX")

  -- 구분자 없는 0xx12345678 → 0xx-XXXX-XXXX
  s, _ = safe_gsub(s, "(%f[%d])(0%d%d)(%d%d%d%d)(%d%d%d%d)(%f[%D])", "%2-XXXX-XXXX")

  return s, s ~= before
end

-- 문자열 하나에 대해 이메일/전화 모두 적용
local function mask_str(s)
  if type(s) ~= "string" then return s, false end
  local changed_any = false
  s, changed_any = mask_emails(s)
  local s2, ch2 = mask_phones(s)
  if ch2 then s = s2; changed_any = true end
  return s, changed_any
end

-- 재귀적으로 record 전체 훑기
local function mask_in_value(v)
  local t = type(v)
  if t == "string" then
    return mask_str(v)
  elseif t == "table" then
    local any = false
    for k, vv in pairs(v) do
      local nv, changed = mask_in_value(vv)
      if changed then v[k] = nv; any = true end
    end
    return v, any
  else
    return v, false
  end
end

function mask_email_phone(tag, ts, record)
  local newrec, changed = mask_in_value(record)
  if changed then newrec["masked.by"] = "lua" end
  return 1, ts, newrec
end
