/**
 * Formatadores de exibição.
 *
 * Os preços vêm do backend como string (ex.: "4500.00") acompanhados de um
 * código de moeda ISO (ex.: "BRL"). Aqui convertemos para Number e usamos
 * Intl.NumberFormat para exibir no padrão da moeda informada (default BRL).
 */

const formatters = new Map<string, Intl.NumberFormat>()

function getFormatter(currency: string): Intl.NumberFormat {
  let fmt = formatters.get(currency)
  if (!fmt) {
    try {
      fmt = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency,
      })
    } catch {
      // Código de moeda inválido — cai para BRL.
      fmt = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
      })
    }
    formatters.set(currency, fmt)
  }
  return fmt
}

/**
 * Formata um preço dado o `amount` (string do backend) e `currency` (ISO).
 * Retorna string já localizada (ex.: "R$ 4.500,00").
 */
export function formatPrice(amount: string, currency = 'BRL'): string {
  const value = Number(amount)
  if (Number.isNaN(value)) {
    return amount
  }
  return getFormatter(currency).format(value)
}
