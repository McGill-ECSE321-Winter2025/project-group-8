import { useState } from "react";

export function DateFilterComponent({ onFilterChange }) {
  const [dateFilter, setDateFilter] = useState("");
  
  const handleDateFilterChange = (e) => {
    const filterValue = e.target.value;
    setDateFilter(filterValue);
    onFilterChange(filterValue);
  };

  return (
    <div className="w-full">
      <select 
        className="w-full p-2 border rounded-lg"
        value={dateFilter}
        onChange={handleDateFilterChange}
      >
        <option value="">Date range</option>
        <option value="this-week">This week</option>
        <option value="this-month">This month</option>
        <option value="next-month">Next month</option>
      </select>
    </div>
  );
}