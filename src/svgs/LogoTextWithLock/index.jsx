import Svg, { Rect, Text as SvgText } from 'react-native-svg'

/**
 * @param {{
 *  width?: string
 *  height?: string
 * }} props
 */
export const LogoTextWithLock = ({ height = '57', width = '256' }) => (
  <Svg
    width={width}
    height={height}
    viewBox="0 0 256 64"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <Rect
      x="4"
      y="4"
      width="56"
      height="56"
      rx="2"
      fill="#14161b"
      stroke="#b08d57"
      strokeWidth="1.5"
    />
    <Rect
      x="14"
      y="14"
      width="36"
      height="36"
      rx="2"
      fill="#08090b"
      stroke="#2a2e36"
      strokeWidth="1"
    />
    <Rect x="25" y="20" width="14" height="14" rx="2" fill="#d4af77" />
    <Rect x="29" y="32" width="6" height="14" rx="1" fill="#d4af77" />
    <SvgText
      x="72"
      y="42"
      fill="#F6F6F6"
      fontSize="26"
      fontWeight="500"
      fontFamily="Inter"
    >
      Lockwright
    </SvgText>
  </Svg>
)
